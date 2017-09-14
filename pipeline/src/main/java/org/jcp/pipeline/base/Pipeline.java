package org.jcp.pipeline.base;

import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.jcp.pipeline.base.exception.PipelineErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline {

    private static final Logger LOG = LoggerFactory.getLogger(Pipeline.class);

    private static final int DEFAULT_SHUTDOWN_TIMEOUT_SECONDS = 60;

    private final ThreadLocal<PipelineOperationThread> pipelineOperationThreadThreadLocal;
    private final HashSet<Operation<?>>                operations;
    private final Semaphore                            semaphore;
    private final PipelineErrorHandler                 pipelineErrorHandler;
    private final ExecutionStatistics                  executionStatistics;

    private final int shutdownTimeout;

    private boolean shutdownFlag = false;

    Pipeline() {
        this(DEFAULT_SHUTDOWN_TIMEOUT_SECONDS);
    }

    Pipeline(final int shutdownTimeout) {
        this((e, operation, parameter) -> {
            throw new RuntimeException("Error performing operation: " + operation + " with parameter " + parameter, e);
        }, shutdownTimeout);
    }

    Pipeline(final PipelineErrorHandler pipelineErrorHandler, final int shutdownTimeout) {
        this.pipelineOperationThreadThreadLocal = new ThreadLocal<>();
        this.operations = new HashSet<>();
        this.semaphore = new Semaphore(1);
        this.pipelineErrorHandler = pipelineErrorHandler;
        this.executionStatistics = new ExecutionStatistics();
        this.shutdownTimeout = shutdownTimeout;
    }

    <T> void addOperation(final Operation<T> operation) {
        operations.add(operation);
    }

    public <T> void executeOperation(final Operation<T> operation, final T parameter) {
        if (shutdownFlag) {
            throw new IllegalStateException("process instance shutdown");
        }
        startThread(operation);
        try {
            execute(operation, parameter);
        } finally {
            finishThread();
        }
    }

    private <T> void execute(final Operation<T> operation, final T parameter) {
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

    <T> void startThread(Operation<T> operation) {
        final PipelineOperationThread existing = pipelineOperationThreadThreadLocal.get();
        if (existing != null) {
            throw new IllegalStateException("The thread has already been started: " + existing);
        }

        pipelineOperationThreadThreadLocal.set(new PipelineOperationThread(operation));
    }

    private void finishThread() {
        pipelineOperationThreadThreadLocal.remove();
    }

    void proceed() {
        resumePostponed();
        finishThread();
    }

    public void resumePostponed() {
        try {
            semaphore.acquire();
            executionStatistics.decrementPostponed();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    public void resumeHeld() {
        executionStatistics.decrementHeld();
    }

    public void postpone() {
        try {
            semaphore.acquire();
            executionStatistics.incrementPostponed();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    public void hold() {
        try {
            semaphore.acquire();
            executionStatistics.incrementHeld();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    public void shutdown() {
        LOG.info("Shutdown requested. Will try to finish all postponed and held operations till the deadline of {} seconds starting from now", shutdownTimeout);
        this.shutdownFlag = true;
        finish();
    }

    public void finish() {
        final long timeout = shutdownTimeout * 1000 + System.currentTimeMillis();
        while (executionStatistics.getHeld() > 0 || executionStatistics.getPostponed() > 0) {
            if (Thread.currentThread().isInterrupted()) {
                LOG.warn("Shutdown was interrupted");
                break;
            }
            if (System.currentTimeMillis() >= timeout) {
                LOG.warn("Shutdown timeout has been reached");
                break;
            }
            finishHeld(timeout);
            finishPostponed(timeout);
        }
    }

    private void finishHeld(final long timeout) {
        while (executionStatistics.getHeld() > 0) {
            if (System.currentTimeMillis() >= timeout) {
                LOG.warn("Shutdown timeout has been reached. Finishing held operations was not completed.");
                break;
            }
            try {
                operations.forEach(op -> {
                    startThread(op);
                    try {
                        op.cleanup();
                    } catch (RuntimeException e) {
                        LOG.error("Cleanup failed", e);
                        pipelineErrorHandler.handle(e, op, null);
                    }
                });
            } finally {
                finishThread();
            }
        }
    }

    private void finishPostponed(final long timeout) {
        try {
            semaphore.acquire();
            executionStatistics.incrementPostponeQueue();
            while (executionStatistics.getHeld() > 0 || executionStatistics.getPostponed() > 0) {
                if (System.currentTimeMillis() >= timeout) {
                    LOG.warn("Shutdown timeout has been reached. Finishing postponed operations was not completed.");
                    break;
                }
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            }
            executionStatistics.decrementPostponeQueue();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }
}
