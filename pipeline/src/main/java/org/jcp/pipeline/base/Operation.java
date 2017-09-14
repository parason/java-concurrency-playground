package org.jcp.pipeline.base;

public abstract class Operation<T> {

    private final Pipeline     pipelineReference;
    private final Operation<T> nextOperationReference;

    protected Operation(final Pipeline pipeline,
            final Operation<T> nextOperationReference) {
        this.pipelineReference = pipeline;
        pipeline.addOperation(this);

        this.nextOperationReference = nextOperationReference;
    }

    public void perform(final T value) {
        doPerform(value);
        if (nextOperationReference != null) {
            nextOperationReference.perform(value);
        }
    }

    protected abstract void doPerform(T value);

    protected Pipeline getPipelineReference() {
        return pipelineReference;
    }

    Operation<T> getNextOperation() {
        return nextOperationReference;
    }

    public abstract void cleanup();

}
