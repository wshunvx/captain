package com.netflix.eureka.dashboard.datasource.entity.rule;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AuthorityRuleEntity extends AbstractRuleEntity<AuthorityRule> {

    public AuthorityRuleEntity() {
    }

    public AuthorityRuleEntity(AuthorityRule authorityRule) {
        AssertUtil.notNull(authorityRule, "Authority rule should not be null");
        this.rule = authorityRule;
    }

    public static AuthorityRuleEntity fromAuthorityRule(String app, AuthorityRule rule) {
        AuthorityRuleEntity entity = new AuthorityRuleEntity(rule);
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
    public int getStrategy() {
        return rule.getStrategy();
    }
}
