package com.netflix.eureka.dashboard.datasource.entity.rule;

import java.util.Date;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;

public abstract class AbstractRuleEntity<T extends AbstractRule> extends RuleEntity {

    protected String app;
    protected String instanceId;

    protected T rule;

    private Date gmtCreate;
    private Date gmtModified;

    @Override
    public String getApp() {
        return app;
    }

    @Override
	public String getInstanceId() {
		return instanceId;
	}
    
	public AbstractRuleEntity<T> setInstanceId(String instanceId) {
		this.instanceId = instanceId;
		return this;
	}
    
	public AbstractRuleEntity<T> setApp(String app) {
        this.app = app;
        return this;
    }

    public T getRule() {
        return rule;
    }

    public AbstractRuleEntity<T> setRule(T rule) {
        this.rule = rule;
        return this;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public AbstractRuleEntity<T> setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
        return this;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public AbstractRuleEntity<T> setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
        return this;
    }

    @Override
    public T toRule() {
        return rule;
    }
}
