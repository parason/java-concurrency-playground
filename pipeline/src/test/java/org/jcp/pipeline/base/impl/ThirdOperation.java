package org.jcp.pipeline.base.impl;

import org.jcp.pipeline.base.Operation;
import org.jcp.pipeline.base.Pipeline;
import org.jcp.pipeline.base.impl.model.TestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdOperation extends Operation<TestEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(SecondOperation.class);

    public ThirdOperation(Pipeline pipeline, Operation<TestEntity> nextOperationReference) {
        super(pipeline, nextOperationReference);
    }

    @Override
    protected void doPerform(TestEntity value) {
        LOG.info("Performing third operation with value {}", value);
        value.incrementCount();
    }

    @Override
    public void cleanup() {
        LOG.info("Cleaning up after third operation");
    }
}
