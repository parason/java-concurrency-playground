package org.jcp.pc.base.process;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.jcp.pc.base.components.DefaultProcessExecutor;
import org.jcp.pc.base.components.TestEntityConsumer;
import org.jcp.pc.base.components.TestEntityProducer;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the process components together
 */
public class ProcessTest {

    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

    private DefaultProcessExecutor defaultProcessExecutor;
    private TestEntityConsumer     testEntityConsumer;
    private TestEntityProducer     testEntityProducer;

    @Before
    public void before() {
        testEntityProducer = new TestEntityProducer();
        testEntityConsumer = new TestEntityConsumer(Executors.newFixedThreadPool(4), SHUTDOWN_TIMEOUT_SECONDS);

        defaultProcessExecutor = new DefaultProcessExecutor(testEntityProducer, testEntityConsumer);
    }


    @Test
    public void testDefault() {
        final Thread thread = new Thread(() -> defaultProcessExecutor.run());
        thread.start();
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
        defaultProcessExecutor.shutdown();
    }


}