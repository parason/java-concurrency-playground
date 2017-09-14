package org.jcp.pc.base.exception;

/**
 * This is an exception handling interface that takes care of the exceptions that could be thrown during producing
 */
public interface ProducerExceptionHandler {
    /**
     * The implementation have to make sure that the exception is processed
     *
     * @param e the exception to be handled
     */
    void handle(final RuntimeException e);
}
