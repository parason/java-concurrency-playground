package org.jcp.pc.base.executor;

import org.jcp.pc.base.process.Producer;
import org.jcp.pc.base.process.WorkingConsumer;

/**
 * Default executor, accepts the {@link Producer} and {@link WorkingConsumer} instances and orchestrates executions
 *
 * @param <T> supported type
 */
public abstract class ProcessExecutor<T> {

    private final Producer<T>        producer;
    private final WorkingConsumer<T> consumer;

    public ProcessExecutor(final Producer<T> producer,
            final WorkingConsumer<T> consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    public void run() {
        producer.acquireTasks(consumer);
    }

    public void shutdown() {
        producer.requestShutdown();
        consumer.requestShutdown();
    }
}
