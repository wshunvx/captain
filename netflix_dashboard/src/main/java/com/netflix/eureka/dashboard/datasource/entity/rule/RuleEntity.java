package com.netflix.eureka.dashboard.datasource.entity.rule;

import java.util.Date;

import com.alibaba.csp.sentinel.slots.block.Rule;

public interface RuleEntity {

	Long getId();

    void setId(Long id);

    String getApp();
    String getInstanceId();

    Date getGmtCreate();
    
    Rule toRule();
    
}
