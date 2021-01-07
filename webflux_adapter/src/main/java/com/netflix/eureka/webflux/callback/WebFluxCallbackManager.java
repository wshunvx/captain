package com.netflix.eureka.webflux.callback;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.alibaba.csp.sentinel.util.AssertUtil;

import org.springframework.web.server.ServerWebExchange;

public final class WebFluxCallbackManager {

    private static final BiFunction<ServerWebExchange, String, String> DEFAULT_URL_CLEANER = (w, url) -> url;
    private static final Function<ServerWebExchange, String> DEFAULT_ORIGIN_PARSER = (w) -> "";

    /**
     * BlockRequestHandler: (serverExchange, exception) -> response
     */
    private static volatile BlockRequestHandler blockHandler = new DefaultBlockRequestHandler();
    /**
     * UrlCleaner: (serverExchange, originalUrl) -> finalUrl
     */
    private static volatile BiFunction<ServerWebExchange, String, String> urlCleaner = DEFAULT_URL_CLEANER;
    /**
     * RequestOriginParser: (serverExchange) -> origin
     */
    private static volatile Function<ServerWebExchange, String> requestOriginParser = DEFAULT_ORIGIN_PARSER;

    public static /*@NonNull*/ BlockRequestHandler getBlockHandler() {
        return blockHandler;
    }

    public static void resetBlockHandler() {
        WebFluxCallbackManager.blockHandler = new DefaultBlockRequestHandler();
    }

    public static void setBlockHandler(BlockRequestHandler blockHandler) {
        AssertUtil.notNull(blockHandler, "blockHandler cannot be null");
        WebFluxCallbackManager.blockHandler = blockHandler;
    }

    public static /*@NonNull*/ BiFunction<ServerWebExchange, String, String> getUrlCleaner() {
        return urlCleaner;
    }

    public static void resetUrlCleaner() {
        WebFluxCallbackManager.urlCleaner = DEFAULT_URL_CLEANER;
    }

    public static void setUrlCleaner(BiFunction<ServerWebExchange, String, String> urlCleaner) {
        AssertUtil.notNull(urlCleaner, "urlCleaner cannot be null");
        WebFluxCallbackManager.urlCleaner = urlCleaner;
    }

    public static /*@NonNull*/ Function<ServerWebExchange, String> getRequestOriginParser() {
        return requestOriginParser;
    }

    public static void resetRequestOriginParser() {
        WebFluxCallbackManager.requestOriginParser = DEFAULT_ORIGIN_PARSER;
    }

    public static void setRequestOriginParser(Function<ServerWebExchange, String> requestOriginParser) {
        AssertUtil.notNull(requestOriginParser, "requestOriginParser cannot be null");
        WebFluxCallbackManager.requestOriginParser = requestOriginParser;
    }

    private WebFluxCallbackManager() {}
}
