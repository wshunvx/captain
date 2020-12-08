package com.netflix.eureka.http.api.route;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import com.netflix.zuul.context.RequestContext;

public class PrefixRoutePathMatcher implements Predicate<RequestContext> {

    private final String pattern;

    private final PathMatcher pathMatcher;
    private final boolean canMatch;

    public PrefixRoutePathMatcher(String pattern) {
        AssertUtil.assertNotBlank(pattern, "pattern cannot be blank");
        this.pattern = pattern;
        this.pathMatcher = new AntPathMatcher();
        this.canMatch = pathMatcher.isPattern(pattern);
    }

    @Override
    public boolean test(RequestContext context) {
        //Solve the problem of prefix matching
        HttpServletRequest request = context.getRequest();
        String path = request.getRequestURI();
        if (path == null) {
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
