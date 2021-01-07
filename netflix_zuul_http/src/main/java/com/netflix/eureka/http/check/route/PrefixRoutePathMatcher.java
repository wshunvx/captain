package com.netflix.eureka.http.check.route;

import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

public class PrefixRoutePathMatcher implements Predicate<Route> {

    private final String pattern;

    private final PathMatcher pathMatcher;
    private final boolean canMatch;
    private final boolean prefixStripped;
    

    public PrefixRoutePathMatcher(String pattern, boolean prefixStripped) {
        AssertUtil.assertNotBlank(pattern, "pattern cannot be blank");
        this.pattern = pattern;
        this.prefixStripped = prefixStripped;
        this.pathMatcher = new AntPathMatcher();
        this.canMatch = pathMatcher.isPattern(pattern);
    }

    @Override
    public boolean test(Route route) {
        //Solve the problem of prefix matching
        String path = route.getPath();
        if(prefixStripped) {
        	path = route.getFullPath();
        }
        if (StringUtils.isEmpty(path)) {
            AssertUtil.assertNotBlank(pattern, "requesturi cannot be blank");
        }
        if (canMatch) {
            return pathMatcher.match(pattern, path);
        }
        return false;
    }

    public String getPattern() {
        return pattern;
    }
}
