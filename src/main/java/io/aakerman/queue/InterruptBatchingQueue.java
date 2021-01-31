package io.aakerman.queue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;

public class InterruptBatchingQueue<T> implements BatchingQueue<T> {

    private Collection<T> batch;

    private final int batchLimit;

    private final long batchTimeout;

    private Consumer<Collection<T>> consumer;

    private Thread timeout;

    private boolean inBatch;

    public InterruptBatchingQueue(long batchTimeout, int batchLimit) {
        this.batchTimeout = batchTimeout;
        this.batchLimit = batchLimit;
        this.inBatch = false;
    }

    @Override
    public synchronized void add(T element) {
        if (batch == null) {
            batch = new LinkedList<>();
        }
        batch.add(element);
        if (batch.size() == batchLimit) {
            timeout.interrupt();
            emit();
        } else if (!inBatch) {
            inBatch = true;
            timeout = new Thread(() -> {
                try {
                    Thread.sleep(batchTimeout);
                    emit();
                } catch (InterruptedException e) { /* ignore */ }
            });
            timeout.start();
        }
    }

    @Override
    public void onBatch(Consumer<Collection<T>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void shutdown() {
        if (batch.size() > 0) {
            timeout.interrupt();
            emit();
        }
    }

    private void emit() {
        consumer.accept(batch);
        batch = new LinkedList<>();
        inBatch = false;
    }
}
