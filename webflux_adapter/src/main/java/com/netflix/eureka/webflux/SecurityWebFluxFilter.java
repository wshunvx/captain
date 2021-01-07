package com.netflix.eureka.webflux;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.eureka.webflux.callback.WebFluxCallbackManager;
import com.netflix.eureka.webflux.reactor.ContextConfig;
import com.netflix.eureka.webflux.reactor.EntryConfig;
import com.netflix.eureka.webflux.reactor.ReactorTransformer;

import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "eureka.security.enabled", matchIfMissing = true)
public class SecurityWebFluxFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Maybe we can get the URL pattern elsewhere via:
        // exchange.getAttributeOrDefault(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, path)
        String path = exchange.getRequest().getPath().value();

        String finalPath = WebFluxCallbackManager.getUrlCleaner().apply(exchange, path);
        if (StringUtil.isEmpty(finalPath)) {
            return chain.filter(exchange);
        }
        return chain.filter(exchange)
            .transform(buildSecurityTransformer(exchange, finalPath));
    }

    private ReactorTransformer<Void> buildSecurityTransformer(ServerWebExchange exchange, String finalPath) {
        String origin = Optional.ofNullable(WebFluxCallbackManager.getRequestOriginParser())
            .map(f -> f.apply(exchange))
            .orElse(EMPTY_ORIGIN);

        return new ReactorTransformer<>(new EntryConfig(finalPath, ResourceTypeConstants.COMMON_WEB,
            EntryType.IN, new ContextConfig(finalPath, origin)));
    }

    private static final String EMPTY_ORIGIN = "";
}
