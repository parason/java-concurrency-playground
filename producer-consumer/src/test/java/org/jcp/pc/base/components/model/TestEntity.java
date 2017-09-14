package org.jcp.pc.base.components.model;

public class TestEntity {
    private final long   id;
    private final String description;

    public TestEntity(long id, String description) {
        this.id = id;
        this.description = description;
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
}
