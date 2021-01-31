## Exploring batching options using standard Java

The idea is to implement a `BatchingQueue` that supports the following methods:
* `BatchingQueue::add(T element)`
* `BatchingQueue::onBatch(Consumer<Collection<T>> consumer)`
* `BatchingQueue::shutdown()`? Not sure about this one - perhaps should be implementation dependent

