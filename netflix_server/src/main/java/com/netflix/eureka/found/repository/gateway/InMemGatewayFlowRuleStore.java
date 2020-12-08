package com.netflix.eureka.found.repository.gateway;

import org.springframework.stereotype.Component;

import com.netflix.eureka.constid.SnowflakeIdWorker;
import com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.netflix.eureka.found.repository.InMemoryRuleRepositoryAdapter;

@Component
public class InMemGatewayFlowRuleStore extends InMemoryRuleRepositoryAdapter<GatewayFlowRuleEntity> {
	private static SnowflakeIdWorker ids = SnowflakeIdWorker.newID(System.currentTimeMillis(), 0, 2);
	
	@Override
	protected Long nextId() {
		return ids.nextId();
	}

}
