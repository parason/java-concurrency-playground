package org.jcp.pc.base.components;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.jcp.pc.base.components.model.TestEntity;
import org.jcp.pc.base.process.Producer;

public class TestEntityProducer extends Producer<TestEntity> {
    @Override
    public Collection<TestEntity> fetch() {
        final List<TestEntity> testEntities = new LinkedList<>();

        for (int i = 0; i < 100; i++) {
            testEntities.add(new TestEntity(i, UUID.randomUUID().toString()));
        }

        return testEntities;
    }
}
