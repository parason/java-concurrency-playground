package org.jcp.pipeline.base;

/**
 * Base pipeline process chunk. The implementations perform a (set of) instructions and pass the processed object to
 * the next step. The very first operation should (but does not have to) be an {@link EntryOperation} that will fire the
 * pipeline process by passing the created pipeline to an {@link java.util.concurrent.Executor}
 *
 * @param <T> the supported type of the object that will be processed.
 */
public abstract class Operation<T> {

    private final Pipeline     pipelineReference;
    private final Operation<T> nextOperationReference;

    /**
     * Default constructor, accepts the main pipeline reference and the next operation to be executed.
     *
     * @param pipeline reference to the main pipeline process
     * @param nextOperationReference reference to the operation that has to be performed right after the current
     *                               operation is executed
     */
    protected Operation(final Pipeline pipeline,
            final Operation<T> nextOperationReference) {
        this.pipelineReference = pipeline;
        pipeline.addOperation(this);

        this.nextOperationReference = nextOperationReference;
    }

    /**
     * Directs the processing of an object
     *
     * @param value the object to be processed
     */
    public void perform(final T value) {
        doPerform(value);
        if (nextOperationReference != null) {
            nextOperationReference.perform(value);
        }
    }

    /**
     * The concrete processing take part here. The implementations have to provide the concrete logic chunk to handle
     * the object.
     *
     * @param value the object to be handled
     */
    protected abstract void doPerform(T value);

    /**
     * Getter for the main pipeline reference
     *
     * @return the pipeline reference
     */
    protected Pipeline getPipelineReference() {
        return pipelineReference;
    }

    /**
     * Next operation reference getter
     * @return the next operation reference
     */
    Operation<T> getNextOperation() {
        return nextOperationReference;
    }

    /**
     * Performs cleanup after if required.
     */
    public abstract void cleanup();

}
