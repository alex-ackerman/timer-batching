package io.aakerman;

import io.aakerman.queue.BatchingQueue;
import io.aakerman.queue.TimedBatchingQueue;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

public class Main {

    public static void main(String[] args) {
        BatchingQueue<Integer> queue = new TimedBatchingQueue<>(300, 5);
        queue.onBatch(batch -> System.out.println(String.format("%s => size %d", batch, batch.size())));

        LongStream randomDelays = new Random().longs(100, 20, 10000);
        Timer timer = new Timer();
        AtomicInteger counter = new AtomicInteger(0);

        randomDelays.forEach(delay -> {
            final int count = counter.getAndIncrement();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    queue.add(count);
                }
            }, delay);
        });

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("shutting down batching queue");
                queue.shutdown();
                timer.cancel();
            }
        }, 11000);

    }

}
