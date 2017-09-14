package org.jcp.pipeline.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostponedOperation<T> implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PostponedOperation.class);

    private final Operation<T> operation;
    private final T            parameter;

    public PostponedOperation(final Operation<T> operation,
            final T parameter) {
        this.operation = operation;
        this.parameter = parameter;
    }

    @Override
    public void run() {
        LOG.trace("Executing postponed operation: {}, parameter: {}", operation, parameter);
        operation.getPipelineReference().startThread(operation);
        try {
            operation.getPipelineReference().executeOperation(operation, parameter);
        } catch (final RuntimeException e) {
            LOG.error("Error executing postponed operation: {}, parameter: {}", operation, parameter, e);
            throw e;
        } finally {
            operation.getPipelineReference().proceed();
        }
    }
}
