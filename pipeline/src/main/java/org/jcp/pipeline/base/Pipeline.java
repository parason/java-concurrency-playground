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

/**
 * Directs the pipeline flow by scheduling execution of the operation sequences, allows pausing operation by putting them
 * "on hold", performs clean ups and takes care of the synchronization.
 * <p>
 * At the moment is not perfect, so one has to take care.
 */
public class Pipeline<T> {

    private static final Logger LOG = LoggerFactory.getLogger(Pipeline.class);

    private final ThreadLocal<PipelineOperationThread> pipelineOperationThreadThreadLocal;
    private final LinkedList<Operation<T>> operations;
    private final Semaphore semaphore;
    private final PipelineErrorHandler pipelineErrorHandler;
    private final Executor executor;
    private final int shutdownTimeout;
    private final AtomicLong onHold;
    private final AtomicLong startedPipelineFlows;

    private boolean shutdownFlag = false;

    /**
     * Creates a {@link Pipeline} instance
     *
     * @param executor             operation execution scheduler
     * @param pipelineErrorHandler error handler
     * @param shutdownTimeout      timeout, in seconds
     */
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

    /**
     * Adds an operation to the pipeline, intended for internal use
     *
     * @param operation {@link Operation} to add
     */
    void addOperation(final Operation<T> operation) {
        assert operation != null;
        operations.add(operation);
    }

    /**
     * Sets the starting point of the workflow
     *
     * @param operation an {@link Operation} that will be executed first in the operation chain
     */
    public void setEntryOperation(final Operation<T> operation) {
        assert operation != null;
        operations.add(operation);
    }

    /**
     * The {@link Executor} then schedules the flow for execution, by calling the #doExecute() method in a new {@link Pipeline}
     * operation thread. In the end of the flow the flow operation counter is decreased. In case if the shutdown was issued, no
     * {@link Pipeline} operation flow executions are allowed.
     *
     * @param parameter the entity to be passed through the pipeline
     */
    public void start(final T parameter) {
        if (shutdownFlag) {
            throw new IllegalStateException("Pipeline shutdown had been initiated, no new operation flows are allowed to be scheduled.");
        }

        // the first operation is an "entry point" and should have been added last
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

    /**
     * At the end of an operations flow decreases the started flows counter taking care of the synchronization.
     */
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

    /**
     * Starts the actual processing flow by calling the actual {@link Operation#perform(Object)}. Makes sure that the
     * operation is performed inside of the {@link Pipeline} flow.
     *
     * @param operation the {@link Operation} tp be performed
     * @param parameter object to be passed to the operation
     */
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

    /**
     * Initializes the {@link ThreadLocal} by sticking a {@link PipelineOperationThread} in it
     *
     * @param operation that will be passed to the {@link PipelineOperationThread}
     */
    private void startThread(Operation<T> operation) {
        final PipelineOperationThread existing = pipelineOperationThreadThreadLocal.get();
        if (existing != null) {
            throw new IllegalStateException("The thread has already been started: " + existing);
        }

        pipelineOperationThreadThreadLocal.set(new PipelineOperationThread(operation));
    }

    /**
     * Finishes the {@link Pipeline} thread by removing the {@link PipelineOperationThread} from the {@link ThreadLocal}
     */
    private void finishThread() {
        pipelineOperationThreadThreadLocal.remove();
    }

    /**
     * Increases the "on hold" operations counter in a synchronous manner.
     * Can be used for queuing the objects that require a batch logic to be performed on them (for example persisting in
     * a database). Till the operation is "on hold" the objects could be collected till a threshold is reached and the
     * batch logic along with the required cleanups is performed. The operation has to be resumed later (see {@link #resume()}).
     */
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

    /**
     * Decreases the "on hold" operations counter.
     */
    public void resume() {
        onHold.decrementAndGet();
    }

    /**
     * Sets the shutdown flag and initializes the cleanup processes. Also waits for the already scheduled operation flows
     * to finish as maximum till the timeout is reached.
     */
    public void shutdown() {
        LOG.info("Shutdown is called, will terminate all remaining operations with the deadline of {} sec.", shutdownTimeout);
        shutdownFlag = true;
        final long timeout = System.currentTimeMillis() + shutdownTimeout * 1000;
        while (onHold.get() > 0 && System.currentTimeMillis() <= timeout) {
            // force cleanup the operations, each in a separate thread
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
