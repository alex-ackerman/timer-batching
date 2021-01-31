package io.aakerman.queue;

import java.util.Collection;
import java.util.function.Consumer;

public interface BatchingQueue<T> {

    void add(T element);

    void onBatch(Consumer<Collection<T>> consumer);

    void shutdown();

}
