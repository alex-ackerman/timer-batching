package io.aakerman.queue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class TimeoutBatchingQueue<T> implements BatchingQueue<T> {

    private Collection<T> batch;

    private final Timer timer;

    private TimerTask task;

    private final long batchTimeout;

    private Consumer<Collection<T>> consumer;

    private final int batchLimit;

    private boolean inBatch;

    public TimeoutBatchingQueue(long batchTimeout, int batchLimit) {
        this.batchTimeout = batchTimeout;
        this.timer = new Timer();
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
            emit();
        } else if (!inBatch) {
            inBatch = true;
            task = new TimerTask() {
                @Override
                public void run() {
                    emit();
                }
            };
            timer.schedule(task, batchTimeout);
        }
    }

    @Override
    public void onBatch(Consumer<Collection<T>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public synchronized void shutdown() {
        if (batch.size() > 0) {
            emit();
        }
        if (task != null) {
            task.cancel();
        }
        timer.cancel();
    }

    private void emit() {
        consumer.accept(batch);
        batch = new LinkedList<>();
        inBatch = false;
        task.cancel();
    }
}
