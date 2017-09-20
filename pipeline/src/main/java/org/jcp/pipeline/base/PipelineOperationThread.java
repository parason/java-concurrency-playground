package org.jcp.pipeline.base;

class PipelineOperationThread {

    private Operation currentOperation;

    <T> PipelineOperationThread(final Operation<T> entry) {
        reset(entry);
    }

    <T> Operation<T> put(final Operation<T> operation) {
        final Operation<T> previousOperation = currentOperation;
        currentOperation = operation;
        return previousOperation;
    }

    <T> void reset(Operation<T> operation) {
        currentOperation = operation;
    }

}
