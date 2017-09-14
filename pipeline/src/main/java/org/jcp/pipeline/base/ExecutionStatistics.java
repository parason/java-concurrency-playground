package org.jcp.pipeline.base;

import java.util.concurrent.atomic.AtomicLong;

class ExecutionStatistics {
    private final AtomicLong held;
    private final AtomicLong postponed;
    private final AtomicLong postponeQueue;

    ExecutionStatistics() {
        this.held = new AtomicLong();
        this.postponed = new AtomicLong();
        this.postponeQueue = new AtomicLong();
    }

    void decrementPostponed() {
        postponed.decrementAndGet();
    }

    void incrementPostponed() {
        postponed.incrementAndGet();
    }

    void incrementHeld() {
        held.incrementAndGet();
    }

    void decrementHeld() {
        held.decrementAndGet();
    }

    void incrementPostponeQueue() {
        postponeQueue.incrementAndGet();
    }

    void decrementPostponeQueue() {
        postponeQueue.decrementAndGet();
    }

    public long getHeld() {
        return held.get();
    }

    public long getPostponed() {
        return postponed.get();
    }

    public long getPostponeQueue() {
        return postponeQueue.get();
    }
}
