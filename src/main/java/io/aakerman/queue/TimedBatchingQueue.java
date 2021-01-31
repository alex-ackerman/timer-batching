package io.aakerman.queue;

import java.util.*;
import java.util.function.Consumer;

public class TimedBatchingQueue<T> implements BatchingQueue<T> {

    private final Collection<T> batch;

    private final Timer timer;

    private TimerTask task;

    private final long batchTimeout;

    private Consumer<Collection<T>> consumer;

    private final int batchLimit;

    private boolean inBatch;

    public TimedBatchingQueue(long batchTimeout, int batchLimit) {
        this.batch = new LinkedList<>();
        this.batchTimeout = batchTimeout;
        this.timer = new Timer();
        this.batchLimit = batchLimit;
        this.inBatch = false;
    }

    @Override
    public synchronized void add(T element) {
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
        consumer.accept(new ArrayList<>(batch));
        batch.clear();
        inBatch = false;
        task.cancel();
    }
}
