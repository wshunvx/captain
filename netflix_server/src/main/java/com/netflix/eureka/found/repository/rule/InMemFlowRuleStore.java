package com.netflix.eureka.found.repository.rule;

import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.netflix.eureka.constid.SnowflakeIdWorker;
import com.netflix.eureka.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.netflix.eureka.found.repository.InMemoryRuleRepositoryAdapter;

@Component
public class InMemFlowRuleStore extends InMemoryRuleRepositoryAdapter<FlowRuleEntity> {

	private static SnowflakeIdWorker ids = SnowflakeIdWorker.newID(System.currentTimeMillis(), 1, 2);
	
	@Override
	protected Long nextId() {
		return ids.nextId();
	}
	
    protected FlowRuleEntity preProcess(FlowRuleEntity entity) {
        if (entity != null && entity.isClusterMode()) {
            ClusterFlowConfig config = entity.getClusterConfig();
            if (config == null) {
                config = new ClusterFlowConfig();
                entity.setClusterConfig(config);
            }
            // Set cluster rule id.
            config.setFlowId(entity.getId());
        }
        return entity;
    }
}
