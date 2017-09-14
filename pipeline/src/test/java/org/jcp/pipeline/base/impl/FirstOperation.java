package org.jcp.pipeline.base.impl;

import org.jcp.pipeline.base.Operation;
import org.jcp.pipeline.base.Pipeline;
import org.jcp.pipeline.base.impl.model.TestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstOperation extends Operation<TestEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(FirstOperation.class);

    public FirstOperation(Pipeline pipeline, Operation<TestEntity> nextOperationReference) {
        super(pipeline, nextOperationReference);
    }

    @Override
    protected void doPerform(TestEntity value) {
        LOG.info("Performing first operation with value {}", value);
        value.incrementCount();
    }

    @Override
    public void cleanup() {
        LOG.info("Cleaning up after first operation");
    }
}
