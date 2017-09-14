package org.jcp.pipeline.base.impl;

import org.jcp.pipeline.base.Operation;
import org.jcp.pipeline.base.Pipeline;
import org.jcp.pipeline.base.impl.model.TestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondOperation extends Operation<TestEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(SecondOperation.class);

    public SecondOperation(Pipeline pipeline, Operation<TestEntity> nextOperationReference) {
        super(pipeline, nextOperationReference);
    }

    @Override
    protected void doPerform(TestEntity value) {
        LOG.info("Performing second operation with value {}", value);
        value.incrementCount();
    }

    @Override
    public void cleanup() {
        LOG.info("Cleaning up after second operation");
    }
}
