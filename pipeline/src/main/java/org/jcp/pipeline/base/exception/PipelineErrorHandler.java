package org.jcp.pipeline.base.exception;

import org.jcp.pipeline.base.Operation;

/**
 * A simple interface for error handling
 */
@FunctionalInterface
public interface PipelineErrorHandler {

    /**
     * Performs the handling
     *
     * @param e         exception that ha been thrown during an operation
     * @param operation the operation
     * @param parameter the operations parameter
     */
    void handle(RuntimeException e, Operation<?> operation, Object parameter);

}
