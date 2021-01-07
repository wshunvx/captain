package com.netflix.eureka.webflux.reactor;

import com.alibaba.csp.sentinel.util.AssertUtil;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;

public class SecurityFluxOperator<T> extends FluxOperator<T, T> {

    private final EntryConfig entryConfig;

    public SecurityFluxOperator(Flux<? extends T> source, EntryConfig entryConfig) {
        super(source);
        AssertUtil.notNull(entryConfig, "entryConfig cannot be null");
        this.entryConfig = entryConfig;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        source.subscribe(new ReactorSubscriber<>(entryConfig, actual, false));
    }
}
