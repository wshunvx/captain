package com.netflix.eureka.found.repository.rule;

import org.springframework.stereotype.Component;

import com.netflix.eureka.constid.SnowflakeIdWorker;
import com.netflix.eureka.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.netflix.eureka.found.repository.InMemoryRuleRepositoryAdapter;

@Component
public class InMemDegradeRuleStore extends InMemoryRuleRepositoryAdapter<DegradeRuleEntity> {
	private static SnowflakeIdWorker ids = SnowflakeIdWorker.newID(System.currentTimeMillis(), 1, 1);
	
	@Override
	protected Long nextId() {
		return ids.nextId();
	}
}
