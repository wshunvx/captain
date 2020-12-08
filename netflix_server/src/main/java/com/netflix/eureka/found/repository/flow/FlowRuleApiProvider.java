package com.netflix.eureka.found.repository.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.netflix.eureka.dashboard.rule.DynamicRuleProvider;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Component("flowRuleDefaultProvider")
public class FlowRuleApiProvider implements DynamicRuleProvider<List<FlowRuleEntity>> {

    @Autowired
    private SentinelApiClient sentinelApiClient;
    @Autowired
    private PeerAwareInstanceRegistry instanceRegistry;

    @Override
    public List<FlowRuleEntity> getRules(String appName) throws Exception {
        if (StringUtil.isBlank(appName)) {
            return new ArrayList<>();
        }
        
        Application application = instanceRegistry.getApplication(appName);
        if(application != null) {
        	List<InstanceInfo> list = application.getInstances().stream()
            .filter(app -> app.getStatus() == InstanceStatus.UP).collect(Collectors.toList());
        	if (!list.isEmpty()) {
        		InstanceInfo machine = list.get(0);
                return sentinelApiClient.fetchFlowRuleOfMachine(machine.getAppName(), machine.getHomePageUrl());
                
            }
        }
        
        return new ArrayList<>();
    }
}
