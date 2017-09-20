package org.jcp.pipeline.base;

import java.util.concurrent.Executor;

/**
 * Entry point of the pipeline flow
 *
 * @param <T> entity type
 *
 * @see Operation for more details
 */
public class EntryOperation<T> extends Operation<T> {

    private final Executor runner;

    /**
     * Default constructor that
     *
     * @param pipeline pipeline instance
     * @param nextOperationReference reference to the next operation that will be performed
     * @param runner {@link Executor} that is responsible for launching the pipeline
     *
     * @see Operation constructor for more details
     */
    public EntryOperation(final Pipeline pipeline,
            final Operation<T> nextOperationReference,
            final Executor runner) {
        super(pipeline, nextOperationReference);
        this.runner = runner;
    }

    @Override
    protected void doPerform(T value) {
        getPipelineReference().postpone();
        runner.execute(new PostponedOperation<>(getNextOperation(), value));
    }

    @Override
    public void cleanup() {
        // do nothing
    }
}
