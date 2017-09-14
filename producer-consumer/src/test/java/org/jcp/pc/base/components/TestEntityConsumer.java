package org.jcp.pc.base.components;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.jcp.pc.base.components.model.TestEntity;
import org.jcp.pc.base.process.WorkingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestEntityConsumer extends WorkingConsumer<TestEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(TestEntityConsumer.class);

    public TestEntityConsumer(final ExecutorService executorService, final int timeout) {
        super(executorService, timeout);
        LOG.info("Created consumer with Executor service of {} and timeout {} seconds", executorService.getClass(), timeout);
    }

    @Override
    public void process(final TestEntity result) {
        LOG.info("Processing entity {}", result);
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        LOG.info("Processing entity {} ended", result);
    }
}
