package com.netflix.eureka.http.api.matcher;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import com.netflix.eureka.command.CommandConstants;
import com.netflix.eureka.common.ApiDefinition;
import com.netflix.eureka.common.ApiPathPredicateItem;
import com.netflix.eureka.http.api.route.ZuulRouteMatchers;
import com.netflix.zuul.context.RequestContext;

public class RequestContextApiMatcher extends AbstractApiMatcher<RequestContext> {

    public RequestContextApiMatcher(ApiDefinition apiDefinition) {
        super(apiDefinition);
    }

    @Override
    protected void initializeMatchers() {
    	ApiPathPredicateItem item = apiDefinition.getPredicateItems();
        if (item != null) {
        	Predicate<RequestContext> predicate = fromApiPredicate(item);
            if (predicate != null) {
                matchers.add(predicate);
            }
        }
    }

    private Predicate<RequestContext> fromApiPredicate(/*@NonNull*/ ApiPathPredicateItem item) {
        return fromApiPathPredicate(item);
    }

    private Predicate<RequestContext> fromApiPathPredicate(/*@Valid*/ ApiPathPredicateItem item) {
        String pattern = item.getPattern();
        if (StringUtil.isBlank(pattern)) {
            return null;
        }
        switch (item.getMatchStrategy()) {
            case CommandConstants.URL_MATCH_STRATEGY_REGEX:
                return ZuulRouteMatchers.regexPath(pattern);
            case CommandConstants.URL_MATCH_STRATEGY_PREFIX:
                return ZuulRouteMatchers.antPath(pattern);
            default:
                return ZuulRouteMatchers.exactPath(pattern);
        }
    }
}
