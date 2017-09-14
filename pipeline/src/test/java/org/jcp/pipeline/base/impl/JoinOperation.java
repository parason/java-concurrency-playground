package org.jcp.pipeline.base.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.jcp.pipeline.base.Operation;
import org.jcp.pipeline.base.Pipeline;
import org.jcp.pipeline.base.impl.model.TestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinOperation extends Operation<TestEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(JoinOperation.class);

    private Semaphore semaphore = new Semaphore(1);

    private final List<TestEntity> testEntities;

    public JoinOperation(Pipeline pipeline, Operation<TestEntity> nextOperationReference) {
        super(pipeline, nextOperationReference);

        this.testEntities = new ArrayList<>();
    }

    @Override
    protected void doPerform(TestEntity value) {
        try {
            semaphore.acquire();
            if (testEntities.isEmpty()) {
                getPipelineReference().hold();
            }
            testEntities.add(value);
            if (testEntities.size() > 10) {
                // commit
                LOG.info("Third operation: commit");
                commit();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void cleanup() {
        LOG.info("Cleaning up after the join operation");
        try {
            semaphore.acquire();
            commit();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    private void commit() {
        testEntities.clear();
        getPipelineReference().resumeHeld();
    }
}
