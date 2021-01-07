package com.netflix.eureka.dashboard.datasource.entity.rule;

import com.alibaba.csp.sentinel.slots.block.Rule;

public abstract class RuleEntity {

	private Long id;
	
	public abstract String getApp();
	public abstract String getInstanceId();

	public abstract Rule toRule();
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
}
