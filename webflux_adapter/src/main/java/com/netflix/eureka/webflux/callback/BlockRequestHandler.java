package com.netflix.eureka.webflux.callback;

import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface BlockRequestHandler {

    /**
     * Handle the blocked request.
     *
     * @param exchange server exchange object
     * @param t block exception
     * @return server response to return
     */
    Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t);
}
