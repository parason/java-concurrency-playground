package org.jcp.pipeline.base;

public class PipelineOperationThread {

    private Operation<?> currentOperation;

    public <T> PipelineOperationThread(final Operation<T> entry) {
        reset(entry);
    }

    public <T> Operation<?> put(final Operation<T> operation) {
        final Operation<?> previousOperation = currentOperation;
        currentOperation = operation;
        return previousOperation;
    }

    public <T> void reset(Operation<T> operation) {
        currentOperation = operation;
    }

}
