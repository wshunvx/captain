package com.netflix.eureka.http.api.matcher;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import com.netflix.eureka.common.ApiDefinition;

public abstract class AbstractApiMatcher<T> implements Predicate<T> {

    protected final String apiName;
    protected final ApiDefinition apiDefinition;
    /**
     * We use {@link com.alibaba.csp.sentinel.util.function.Predicate} here as the min JDK version is 1.7.
     */
    protected final Set<Predicate<T>> matchers = new HashSet<>();

    public AbstractApiMatcher(ApiDefinition apiDefinition) {
        AssertUtil.notNull(apiDefinition, "apiDefinition cannot be null");
        AssertUtil.assertNotBlank(apiDefinition.getApiName(), "apiName cannot be empty");
        this.apiName = apiDefinition.getApiName();
        this.apiDefinition = apiDefinition;

        try {
            initializeMatchers();
        } catch (Exception ex) {
            RecordLog.warn("[GatewayApiMatcher] Failed to initialize internal matchers", ex);
        }
    }

    /**
     * Initialize the matchers.
     */
    protected abstract void initializeMatchers();

    @Override
    public boolean test(T t) {
        for (Predicate<T> matcher : matchers) {
            if (matcher.test(t)) {
                return true;
            }
        }
        return false;
    }

    public String getApiName() {
        return apiName;
    }

    public ApiDefinition getApiDefinition() {
        return apiDefinition;
    }
}
