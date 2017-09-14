package org.jcp.pc.base.process;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

import org.jcp.pc.base.exception.ProducerExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jcp.pc.base.exception.UnexpectedCallException;

/**
 * An abstract Producer. Pulls entities and feeds them to the provided {@link WorkingConsumer} in a sync manner.
 *
 * @param <T> supported type
 * @see WorkingConsumer
 */
public abstract class Producer<T> {

    private static final Logger LOG = LoggerFactory.getLogger(Producer.class);

    private final State                    state;
    private final ProducerExceptionHandler producerExceptionHandler;

    /**
     * Default constructor with a basic exception handler (no handle, re-throw)
     */
    public Producer() {
        this(e -> {
            throw e;
        });
    }

    /**
     * Creates a {@link Producer} with a unique {@link State} and specified {@link ProducerExceptionHandler}
     *
     * @param producerExceptionHandler the exception handler
     */
    public Producer(final ProducerExceptionHandler producerExceptionHandler) {
        assert producerExceptionHandler != null;
        this.state = new State();
        this.producerExceptionHandler = producerExceptionHandler;
    }

    /**
     * Fetches a collection of objects
     *
     * @return the fetch result
     */
    public abstract Collection<T> fetch();

    /**
     * @param consumer the corresponding {@link Consumer} entity
     */
    public void acquireTasks(final WorkingConsumer<T> consumer) {
        if (state.inProgress.compareAndSet(false, true)) {
            try {
                final Collection<T> fetchResults = fetch();
                if (fetchResults == null || fetchResults.isEmpty()) {
                    LOG.info("No entities found for processing");
                    return;
                }

                LOG.info("Found {} items for processing", fetchResults.size());

                for (final T result : fetchResults) {
                    if (state.shutdown.get()) {
                        LOG.info("Shutdown has been requested, finishing the process loop");
                        return;
                    }

                    consumer.accept(result);
                }

            } catch (final RuntimeException e) {
                LOG.error("Exception during processing the produced items", e);
                producerExceptionHandler.handle(e);
            } finally {
                state.inProgress.set(false);
            }
        } else {
            // someone tries to call this method concurrently
            throw new UnexpectedCallException("Invocation on working producer is not allowed");
        }
    }

    /**
     * Informs the producer that the shutdown is requested and and waits until current fetch result is processed
     */
    public void requestShutdown() {
        LOG.info("Requesting shutdown...");
        state.shutdown.set(true);
        while (state.inProgress.get()) {
            LOG.info("Waiting until all tasks are finished...");
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        }
        LOG.info("Producer shutdown complete");
    }

    /**
     * Informs the caller whether the producer is currently executing and is not going to shutdown
     *
     * @return the producers state, see above
     */
    public boolean isRunning() {
        return state.inProgress.get() && !state.shutdown.get();
    }

    /**
     * An aggregate object, that incapsulates the execution state flags of the {@link Producer}
     */
    class State {
        /**
         * Indicates if the {@link Producer} is performing the processing
         */
        private final AtomicBoolean inProgress;
        /**
         * The shutdown flag. When set, the processing loop has to be broken
         */
        private final AtomicBoolean shutdown;

        /**
         * Create a state object that is unique for this {@link Producer}
         */
        private State() {
            this.inProgress = new AtomicBoolean(false);
            this.shutdown = new AtomicBoolean(false);
        }

        public boolean isShuttingDown() {
            return shutdown.get();
        }

        public boolean isInProgress() {
            return inProgress.get();
        }
    }

}
