package com.netflix.eureka.dashboard.rule;

public interface DynamicRulePublisher<T> {

    /**
     * Publish rules to remote rule configuration center for given application name.
     *
     * @param app app name
     * @param rules list of rules to push
     * @throws Exception if some error occurs
     */
    void publish(String app, T rules) throws Exception;
}
