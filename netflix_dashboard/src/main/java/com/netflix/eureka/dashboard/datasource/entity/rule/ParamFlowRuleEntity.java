package com.netflix.eureka.dashboard.datasource.entity.rule;

import java.util.List;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netflix.eureka.common.ParamFlowClusterConfig;
import com.netflix.eureka.common.ParamFlowItem;
import com.netflix.eureka.common.ParamFlowRule;

public class ParamFlowRuleEntity extends AbstractRuleEntity<ParamFlowRule> {

    public ParamFlowRuleEntity() {
    }

    public ParamFlowRuleEntity(ParamFlowRule rule) {
        AssertUtil.notNull(rule, "Authority rule should not be null");
        this.rule = rule;
    }

    public static ParamFlowRuleEntity fromAuthorityRule(String app, ParamFlowRule rule) {
        ParamFlowRuleEntity entity = new ParamFlowRuleEntity(rule);
        entity.setApp(app);
        return entity;
    }

    @JsonIgnore
    public String getLimitApp() {
        return rule.getLimitApp();
    }

    @JsonIgnore
    public String getResource() {
        return rule.getResource();
    }

    @JsonIgnore
    public int getGrade() {
        return rule.getGrade();
    }

    @JsonIgnore
    public Integer getParamIdx() {
        return rule.getParamIdx();
    }

    @JsonIgnore
    public double getCount() {
        return rule.getCount();
    }

    @JsonIgnore
    public List<ParamFlowItem> getParamFlowItemList() {
        return rule.getParamFlowItemList();
    }

    @JsonIgnore
    public int getControlBehavior() {
        return rule.getControlBehavior();
    }

    @JsonIgnore
    public int getMaxQueueingTimeMs() {
        return rule.getMaxQueueingTimeMs();
    }

    @JsonIgnore
    public int getBurstCount() {
        return rule.getBurstCount();
    }

    @JsonIgnore
    public long getDurationInSec() {
        return rule.getDurationInSec();
    }

    @JsonIgnore
    public boolean isClusterMode() {
        return rule.isClusterMode();
    }

    @JsonIgnore
    public ParamFlowClusterConfig getClusterConfig() {
        return rule.getClusterConfig();
    }
}
