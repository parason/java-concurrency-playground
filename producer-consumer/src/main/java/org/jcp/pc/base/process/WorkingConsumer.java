package org.jcp.pc.base.process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Consumer that after receiving a fetch result from the {@link Producer} schedule processing of this result
 *
 * @param <T> the supported type
 */
public abstract class WorkingConsumer<T> implements Consumer<T> {

    private static final Logger LOG = LoggerFactory.getLogger(WorkingConsumer.class);

    private final ExecutorService executorService;
    private final int             timeout;

    /**
     * Creates an instance of the {@link WorkingConsumer} with the specified params.
     *
     * @param executorService service to push the processing
     * @param timeout         shutdown timeout in seconds after which the processing is forcibly interrupted
     */
    public WorkingConsumer(final ExecutorService executorService,
            final int timeout) {
        this.executorService = executorService;
        this.timeout = timeout;
    }

    /**
     * Performs the actual processing
     *
     * @param result the fetch result from {@link Producer}
     */
    public abstract void process(final T result);

    /**
     * Overrides the accept method of {@link Consumer} and schedules the parameter item for processing
     *
     * @param result to be processed
     */
    @Override
    public void accept(final T result) {
        executorService.submit(() -> process(result));
    }

    /**
     * Executes the shutdown
     */
    public void requestShutdown() {
        LOG.info("WorkingConsumer shutdown requested");
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                // shutdown failed
                throw new InterruptedException("Failed to process remaining tasks. Shutdown failed.");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOG.info("WorkingConsumer shutdown complete");
    }

}
