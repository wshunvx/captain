package com.netflix.eureka.found.repository.flow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.netflix.eureka.dashboard.rule.DynamicRulePublisher;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Component("flowRuleDefaultPublisher")
public class FlowRuleApiPublisher implements DynamicRulePublisher<List<FlowRuleEntity>> {

    @Autowired
    private SentinelApiClient sentinelApiClient;
    @Autowired
    private PeerAwareInstanceRegistry instanceRegistry;

    @Override
    public void publish(String app, List<FlowRuleEntity> rules) throws Exception {
        if (StringUtil.isBlank(app)) {
            return;
        }
        if (rules == null) {
            return;
        }
        Application appInfo = instanceRegistry.getApplication(app);
        if (appInfo == null || appInfo.getInstances() == null) {
            return;
        }
        
        List<InstanceInfo> set = appInfo.getInstances();

        for (InstanceInfo machine : set) {
            if (machine.getStatus() != InstanceStatus.UP) {
                continue;
            }
            // TODO: parse the results
            sentinelApiClient.setFlowRuleOfMachine(app, machine.getHomePageUrl(), rules);
        }
    }
}
