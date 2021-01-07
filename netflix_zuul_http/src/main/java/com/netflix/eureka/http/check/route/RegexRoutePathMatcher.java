package com.netflix.eureka.http.check.route;

import java.util.regex.Pattern;

import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

public class RegexRoutePathMatcher implements Predicate<Route> {

    private final String pattern;
    private final Pattern regex;
    private final boolean prefixStripped;
    
    public RegexRoutePathMatcher(String pattern, boolean prefixStripped) {
        AssertUtil.assertNotBlank(pattern, "pattern cannot be blank");
        this.pattern = pattern;
        this.prefixStripped = prefixStripped;
        this.regex = Pattern.compile(pattern);
    }

    @Override
    public boolean test(Route route) {
        //Solve the problem of route matching
    	String path = route.getPath();
        if(prefixStripped) {
        	path = route.getFullPath();
        }
        if (StringUtils.isEmpty(path)) {
            AssertUtil.assertNotBlank(pattern, "requesturi cannot be blank");
        }
        return regex.matcher(path).matches();
    }

    public String getPattern() {
        return pattern;
    }
}