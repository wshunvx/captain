package com.netflix.eureka.dashboard.rule;

public interface DynamicRuleProvider<T> {

    T getRules(String appName) throws Exception;
}
