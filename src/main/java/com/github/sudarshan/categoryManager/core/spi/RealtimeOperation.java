package com.github.sudarshan.categoryManager.core.spi;

import java.util.HashSet;

public interface RealtimeOperation<K,V> {

    public void delete(HashSet<K> ids);
    public V update(V value);
    public V add(V node);
}
