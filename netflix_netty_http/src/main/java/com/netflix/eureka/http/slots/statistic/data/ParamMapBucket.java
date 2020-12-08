package com.netflix.eureka.http.slots.statistic.data;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.netflix.eureka.http.slots.block.RollingParamEvent;
import com.netflix.eureka.http.slots.statistic.cache.CacheMap;
import com.netflix.eureka.http.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;

public class ParamMapBucket {

    private final CacheMap<Object, AtomicInteger>[] data;

    @SuppressWarnings("unchecked")
    public ParamMapBucket() {
        RollingParamEvent[] events = RollingParamEvent.values();
        this.data = new CacheMap[events.length];
        for (RollingParamEvent event : events) {
            data[event.ordinal()] = new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>();
        }
    }

    public void reset() {
        for (RollingParamEvent event : RollingParamEvent.values()) {
            data[event.ordinal()].clear();
        }
    }

    public int get(RollingParamEvent event, Object value) {
        AtomicInteger counter = data[event.ordinal()].get(value);
        return counter == null ? 0 : counter.intValue();
    }

    public ParamMapBucket add(RollingParamEvent event, int count, Object value) {
        AtomicInteger counter = data[event.ordinal()].get(value);
        // Note: not strictly concise.
        if (counter == null) {
            AtomicInteger old = data[event.ordinal()].putIfAbsent(value, new AtomicInteger(count));
            if (old != null) {
                old.addAndGet(count);
            }
        } else {
            counter.addAndGet(count);
        }
        return this;
    }

    public Set<Object> ascendingKeySet(RollingParamEvent type) {
        return data[type.ordinal()].keySet(true);
    }

    public Set<Object> descendingKeySet(RollingParamEvent type) {
        return data[type.ordinal()].keySet(false);
    }

    public static final int DEFAULT_MAX_CAPACITY = 200;
}