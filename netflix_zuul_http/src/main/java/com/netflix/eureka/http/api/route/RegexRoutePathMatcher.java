package com.netflix.eureka.http.api.route;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import com.netflix.zuul.context.RequestContext;

public class RegexRoutePathMatcher implements Predicate<RequestContext> {

    private final String pattern;
    private final Pattern regex;

    public RegexRoutePathMatcher(String pattern) {
        AssertUtil.assertNotBlank(pattern, "pattern cannot be blank");
        this.pattern = pattern;
        this.regex = Pattern.compile(pattern);
    }

    @Override
    public boolean test(RequestContext context) {
        //Solve the problem of route matching
        HttpServletRequest request = context.getRequest();
        String path = request.getRequestURI();
        if (path == null) {
            AssertUtil.assertNotBlank(pattern, "requesturi cannot be blank");
        }
        return regex.matcher(path).matches();
    }

    public String getPattern() {
        return pattern;
    }
}