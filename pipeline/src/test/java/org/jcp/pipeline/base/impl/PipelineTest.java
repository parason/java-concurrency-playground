package org.jcp.pipeline.base.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jcp.pipeline.base.Operation;
import org.jcp.pipeline.base.Pipeline;
import org.jcp.pipeline.base.impl.model.TestEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineTest {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineTest.class);

    private Pipeline pipeline;
    private Operation<TestEntity> entry;
    private ThreadPoolExecutor executor;

    @Before
    public void before() {
        executor = new ThreadPoolExecutor(4, 4, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(50), (r, executor) -> {
            LOG.info("Execution rejected: {}", r);
        });

        pipeline = new Pipeline(executor, (e, operation, parameter) -> {
            throw e;
        }, 60);

        Operation<TestEntity> op4 = new JoinOperation(pipeline, null);
        Operation<TestEntity> op3 = new ThirdOperation(pipeline, op4);
        Operation<TestEntity> op2 = new SecondOperation(pipeline, op3);
        Operation<TestEntity> op1 = new FirstOperation(pipeline, op2);

        pipeline.setEntryOperation(op1);

    }

    @Test
    public void testPipline() {

        final List<TestEntity> testEntities = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            final TestEntity testEntity = new TestEntity(i, UUID.randomUUID().toString());
            testEntities.add(testEntity);
            LOG.info("Scheduling entity {}", testEntity);
            pipeline.start(testEntity);
        }

        pipeline.shutdown();

        testEntities.forEach(te -> Assert.assertEquals(3, te.getUpdateCount()));
    }

}