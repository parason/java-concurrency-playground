package org.jcp.pipeline.base;

import org.jcp.pipeline.base.exception.PipelineErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public class Pipeline<T> {

    private static final Logger LOG = LoggerFactory.getLogger(Pipeline.class);

    public static final int DEFAULT_SHUTDOWN_TIMEOUT_SECONDS = 60;

    private final ThreadLocal<PipelineOperationThread> pipelineOperationThreadThreadLocal;
    private final LinkedList<Operation<T>> operations;
    private final Semaphore semaphore;
    private final PipelineErrorHandler pipelineErrorHandler;
    private final Executor executor;
    private final int shutdownTimeout;
    private final AtomicLong onHold;
    private final AtomicLong startedPipelineFlows;

    private boolean shutdownFlag = false;

    public Pipeline(final Executor executor,
                    final PipelineErrorHandler pipelineErrorHandler,
                    final int shutdownTimeout) {
        this.pipelineOperationThreadThreadLocal = new ThreadLocal<>();
        this.operations = new LinkedList<>();
        this.semaphore = new Semaphore(1);
        this.pipelineErrorHandler = pipelineErrorHandler;
        this.shutdownTimeout = shutdownTimeout;
        this.executor = executor;
        this.onHold = new AtomicLong(0);
        this.startedPipelineFlows = new AtomicLong(0);
    }

    void addOperation(final Operation<T> operation) {
        assert operation != null;
        operations.add(operation);
    }

    public void setEntryOperation(final Operation<T> operation) {
        assert operation != null;
        operations.add(operation);
    }

    public void start(final T parameter) {
        if (shutdownFlag) {
            throw new IllegalStateException("process instance shutdown");
        }

        final Operation<T> first = operations.getLast();

        startedPipelineFlows.incrementAndGet();

        executor.execute(() -> {
            startThread(first);
            try {
                doExecute(first, parameter);
            } finally {
                decrementStarted();
                finishThread();
            }
        });
    }

    private void decrementStarted() {
        try {
            semaphore.acquire();
            startedPipelineFlows.decrementAndGet();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    private void doExecute(final Operation<T> operation, final T parameter) {
        final PipelineOperationThread pipelineOperationThread = pipelineOperationThreadThreadLocal.get();
        if (pipelineOperationThread == null) {
            throw new IllegalArgumentException("This method is not allowed to be called outside of a Pipeline thread.");
        }

        final Operation<?> previousOperation = pipelineOperationThread.put(operation);
        try {
            operation.perform(parameter);
        } catch (final RuntimeException e) {
            pipelineErrorHandler.handle(e, operation, parameter);
        } finally {
            pipelineOperationThread.reset(previousOperation);
        }
    }

    private void startThread(Operation<T> operation) {
        final PipelineOperationThread existing = pipelineOperationThreadThreadLocal.get();
        if (existing != null) {
            throw new IllegalStateException("The thread has already been started: " + existing);
        }

        pipelineOperationThreadThreadLocal.set(new PipelineOperationThread(operation));
    }

    private void finishThread() {
        pipelineOperationThreadThreadLocal.remove();
    }

    public void hold() {
        try {
            semaphore.acquire();
            onHold.incrementAndGet();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    public void resume() {
        onHold.decrementAndGet();
    }

    public void shutdown() {
        LOG.info("Shutdown is called, will terminate all remaining operations with the deadline of {} sec.", shutdownTimeout);
        shutdownFlag = true;
        final long timeout = System.currentTimeMillis() + shutdownTimeout * 1000;
        while (onHold.get() > 0 && System.currentTimeMillis() <= timeout) {
            for (Operation<T> op : operations) {
                startThread(op);
                try {
                    op.cleanup();
                } catch (RuntimeException e) {
                    pipelineErrorHandler.handle(e, op, null);
                } finally {
                    finishThread();
                }
            }
            // wait for the started flows to finish
            if (startedPipelineFlows.get() > 0) {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            }
        }
    }
}
