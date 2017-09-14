package org.jcp.pipeline.base.exception;

import org.jcp.pipeline.base.Operation;

public interface PipelineErrorHandler {

    void handle(RuntimeException e, Operation<?> operation, Object parameter);

}
