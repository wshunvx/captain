package com.netflix.eureka.http.slots.statistic.cache;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;

public class ConcurrentLinkedHashMapWrapper<T, R> implements CacheMap<T, R> {

    protected final ConcurrentMap<T, R> paramStatusMap = CacheBuilder
            .newBuilder().initialCapacity(500)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .<T, R>build().asMap();
    
    @Override
    public boolean containsKey(T key) {
        return paramStatusMap.containsKey(key);
    }

    @Override
    public R get(T key) {
        return paramStatusMap.get(key);
    }

    @Override
    public R remove(T key) {
        return paramStatusMap.remove(key);
    }

    @Override
    public R put(T key, R value) {
        return paramStatusMap.put(key, value);
    }

    @Override
    public R putIfAbsent(T key, R value) {
        return paramStatusMap.putIfAbsent(key, value);
    }

    @Override
    public long size() {
        return paramStatusMap.size();
    }

    @Override
    public void clear() {
    	paramStatusMap.clear();
    }

    @Override
    public Set<T> keySet(boolean ascending) {
    	return paramStatusMap.keySet();
    }
}
