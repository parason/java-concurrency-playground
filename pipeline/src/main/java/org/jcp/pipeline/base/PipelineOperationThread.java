package org.jcp.pipeline.base;

class PipelineOperationThread {

    private Operation currentOperation;

    PipelineOperationThread(final Operation entry) {
        reset(entry);
    }

    Operation put(final Operation operation) {
        final Operation previousOperation = currentOperation;
        currentOperation = operation;
        return previousOperation;
    }

    void reset(Operation operation) {
        currentOperation = operation;
    }

}
