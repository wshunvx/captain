package com.netflix.eureka.http.api.route;

import com.alibaba.csp.sentinel.util.function.Predicate;
import com.netflix.zuul.context.RequestContext;

public final class ZuulRouteMatchers {

    public static Predicate<RequestContext> all() {
        return new Predicate<RequestContext>() {
            @Override
            public boolean test(RequestContext requestContext) {
                return true;
            }
        };
    }

    public static Predicate<RequestContext> antPath(String pathPattern) {
        return new PrefixRoutePathMatcher(pathPattern);
    }

    public static Predicate<RequestContext> exactPath(final String path) {
        return new Predicate<RequestContext>() {
            @Override
            public boolean test(RequestContext exchange) {
                return exchange.getRequest().getServletPath().equals(path);
            }
        };
    }

    public static Predicate<RequestContext> regexPath(String pathPattern) {
        return new RegexRoutePathMatcher(pathPattern);
    }

    private ZuulRouteMatchers() {}
}
