package org.jcp.pipeline.base;

import java.util.concurrent.Executor;

public class EntryOperation<T> extends Operation<T> {

    private final Executor runner;

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
