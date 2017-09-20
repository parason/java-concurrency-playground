package org.jcp.pipeline.base;

/**
 *  to be put into {@link ThreadLocal}
 *
 * @param <T> supported entity type
 */
public class PipelineOperationThread<T> {

    private Operation<?> currentOperation;

    public PipelineOperationThread(final Operation<T> entry) {
        reset(entry);
    }

    public Operation<?> put(final Operation<T> operation) {
        final Operation<?> previousOperation = currentOperation;
        currentOperation = operation;
        return previousOperation;
    }

    public void reset(Operation<T> operation) {
        currentOperation = operation;
    }

}
