package com.netflix.eureka.http.check.route;

import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

public final class ZuulRouteMatchers {

    public static Predicate<Route> all() {
        return new Predicate<Route>() {
            @Override
            public boolean test(Route route) {
                return true;
            }
        };
    }

    public static Predicate<Route> antPath(String pathPattern, boolean prefixStripped) {
        return new PrefixRoutePathMatcher(pathPattern, prefixStripped);
    }

    public static Predicate<Route> exactPath(final String pathPattern, boolean prefixStripped) {
        return new Predicate<Route>() {
            @Override
            public boolean test(Route route) {
            	String path = route.getPath();
                if(prefixStripped) {
                	path = route.getFullPath();
                }
                if (StringUtils.isEmpty(path)) {
                    AssertUtil.assertNotBlank(pathPattern, "requesturi cannot be blank");
                }
                return path.equals(pathPattern);
            }
        };
    }

    public static Predicate<Route> regexPath(String pathPattern, boolean prefixStripped) {
        return new RegexRoutePathMatcher(pathPattern, prefixStripped);
    }

    private ZuulRouteMatchers() {}
}
