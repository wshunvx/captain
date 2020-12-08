package com.netflix.eureka.found.repository.rule;

import org.springframework.stereotype.Component;

import com.netflix.eureka.common.ParamFlowClusterConfig;
import com.netflix.eureka.constid.SnowflakeIdWorker;
import com.netflix.eureka.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.netflix.eureka.found.repository.InMemoryRuleRepositoryAdapter;

@Component
public class InMemParamFlowRuleStore extends InMemoryRuleRepositoryAdapter<ParamFlowRuleEntity> {

	private static SnowflakeIdWorker ids = SnowflakeIdWorker.newID(System.currentTimeMillis(), 1, 3);
	
	@Override
	protected Long nextId() {
		return ids.nextId();
	}

    protected ParamFlowRuleEntity preProcess(ParamFlowRuleEntity entity) {
        if (entity != null && entity.isClusterMode()) {
            ParamFlowClusterConfig config = entity.getClusterConfig();
            if (config == null) {
                config = new ParamFlowClusterConfig();
            }
            // Set cluster rule id.
            config.setFlowId(entity.getId());
        }
        return entity;
    }
}
