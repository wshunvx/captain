package com.netflix.eureka.webflux.reactor;

import com.alibaba.csp.sentinel.util.AssertUtil;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;

public class MonoSentinelOperator<T> extends MonoOperator<T, T> {

    private final EntryConfig entryConfig;

    public MonoSentinelOperator(Mono<? extends T> source, EntryConfig entryConfig) {
        super(source);
        AssertUtil.notNull(entryConfig, "entryConfig cannot be null");
        this.entryConfig = entryConfig;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        source.subscribe(new ReactorSubscriber<>(entryConfig, actual, true));
    }
}
