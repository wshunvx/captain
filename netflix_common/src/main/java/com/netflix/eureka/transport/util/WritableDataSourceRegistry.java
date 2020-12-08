package com.netflix.eureka.transport.util;

import java.util.List;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.netflix.eureka.common.ApiDefinition;
import com.netflix.eureka.common.GatewayFlowRule;
import com.netflix.eureka.common.ParamFlowRule;
import com.netflix.eureka.datasource.WritableDataSource;

public final class WritableDataSourceRegistry {

    private static WritableDataSource<List<FlowRule>> flowDataSource = null;
    private static WritableDataSource<List<AuthorityRule>> authorityDataSource = null;
    private static WritableDataSource<List<DegradeRule>> degradeDataSource = null;
    private static WritableDataSource<List<SystemRule>> systemSource = null;
    private static WritableDataSource<List<ParamFlowRule>> paramFlowWds = null;
    private static WritableDataSource<List<ApiDefinition>> apiDefinitionWds = null;
    private static WritableDataSource<List<GatewayFlowRule>> gatewayFlowWds = null;

    public static synchronized void registerFlowDataSource(WritableDataSource<List<FlowRule>> datasource) {
        flowDataSource = datasource;
    }

    public static synchronized void registerAuthorityDataSource(WritableDataSource<List<AuthorityRule>> dataSource) {
        authorityDataSource = dataSource;
    }

    public static synchronized void registerDegradeDataSource(WritableDataSource<List<DegradeRule>> dataSource) {
        degradeDataSource = dataSource;
    }

    public static synchronized void registerSystemDataSource(WritableDataSource<List<SystemRule>> dataSource) {
        systemSource = dataSource;
    }
    
    public static synchronized void registerParamFlowWdsSource(WritableDataSource<List<ParamFlowRule>> dataSource) {
    	paramFlowWds = dataSource;
    }

    public static synchronized void registerApiDefinitionWds(WritableDataSource<List<ApiDefinition>> dataSource) {
    	apiDefinitionWds = dataSource;
    }
    
    public static synchronized void registerGatewayFlowRule(WritableDataSource<List<GatewayFlowRule>> dataSource) {
    	gatewayFlowWds = dataSource;
    }
    
    public static WritableDataSource<List<FlowRule>> getFlowDataSource() {
        return flowDataSource;
    }

    public static WritableDataSource<List<AuthorityRule>> getAuthorityDataSource() {
        return authorityDataSource;
    }

    public static WritableDataSource<List<DegradeRule>> getDegradeDataSource() {
        return degradeDataSource;
    }

    public static WritableDataSource<List<SystemRule>> getSystemSource() {
        return systemSource;
    }
    
    public static WritableDataSource<List<ApiDefinition>> getApiDefinitionWds() {
        return apiDefinitionWds;
    }

    public static WritableDataSource<List<ParamFlowRule>> getParamFlowWds() {
        return paramFlowWds;
    }
    
    public static WritableDataSource<List<GatewayFlowRule>> getGatewayFlowWds() {
        return gatewayFlowWds;
    }
    
}
