package com.netflix.eureka.found.repository.gateway;

import org.springframework.stereotype.Component;

import com.netflix.eureka.constid.SnowflakeIdWorker;
import com.netflix.eureka.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.netflix.eureka.found.repository.InMemoryRuleRepositoryAdapter;

@Component
public class InMemApiDefinitionStore extends InMemoryRuleRepositoryAdapter<ApiDefinitionEntity> {
	private static SnowflakeIdWorker ids = SnowflakeIdWorker.newID(System.currentTimeMillis(), 0, 1);
	
	@Override
	protected Long nextId() {
		return ids.nextId();
	}

}
