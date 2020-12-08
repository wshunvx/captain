package com.netflix.eureka.http.slots.statistic.cache;

import java.util.Set;

public interface CacheMap<K, V> {

    boolean containsKey(K key);

    V get(K key);

    V remove(K key);

    V put(K key, V value);

    V putIfAbsent(K key, V value);

    long size();

    void clear();

    Set<K> keySet(boolean ascending);
}
