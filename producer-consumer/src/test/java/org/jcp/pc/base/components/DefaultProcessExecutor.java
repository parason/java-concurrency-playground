package org.jcp.pc.base.components;

import org.jcp.pc.base.components.model.TestEntity;
import org.jcp.pc.base.process.Producer;
import org.jcp.pc.base.process.WorkingConsumer;
import org.jcp.pc.base.executor.ProcessExecutor;

public class DefaultProcessExecutor extends ProcessExecutor<TestEntity> {


    public DefaultProcessExecutor(final Producer<TestEntity> producer, final WorkingConsumer<TestEntity> consumer) {
        super(producer, consumer);
    }
}
