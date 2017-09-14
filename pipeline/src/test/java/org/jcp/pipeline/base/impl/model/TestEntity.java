package org.jcp.pipeline.base.impl.model;

public class TestEntity {
    private final long   id;
    private final String description;

    private int updateCount;

    public TestEntity(long id, String description) {
        this.id = id;
        this.description = description;
        this.updateCount = 0;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return "{\"id\":" + id + ", \"description\":" + description + "}";
    }

    public void incrementCount() {
        updateCount += 1;
    }

    public int getUpdateCount() {
        return updateCount;
    }

}
