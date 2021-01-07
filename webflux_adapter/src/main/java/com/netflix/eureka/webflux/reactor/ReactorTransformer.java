package com.netflix.eureka.webflux.reactor;

import java.util.function.Function;

import com.alibaba.csp.sentinel.util.AssertUtil;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactorTransformer<T> implements Function<Publisher<T>, Publisher<T>> {

    private final EntryConfig entryConfig;

    public ReactorTransformer(String resourceName) {
        this(new EntryConfig(resourceName));
    }

    public ReactorTransformer(EntryConfig entryConfig) {
        AssertUtil.notNull(entryConfig, "entryConfig cannot be null");
        this.entryConfig = entryConfig;
    }

    @Override
    public Publisher<T> apply(Publisher<T> publisher) {
        if (publisher instanceof Mono) {
            return new MonoSentinelOperator<>((Mono<T>) publisher, entryConfig);
        }
        if (publisher instanceof Flux) {
            return new SecurityFluxOperator<>((Flux<T>) publisher, entryConfig);
        }

        throw new IllegalStateException("Publisher type is not supported: " + publisher.getClass().getCanonicalName());
    }
}