package com.netflix.eureka.found.resources.actuator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.command.Resource;
import com.netflix.eureka.command.Resource.RuleType;
import com.netflix.eureka.common.ParamFlowClusterConfig;
import com.netflix.eureka.common.ParamFlowItem;
import com.netflix.eureka.common.ParamFlowRule;
import com.netflix.eureka.dashboard.client.HttpapiClient;
import com.netflix.eureka.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.repository.RuleRepositoryAdapter;
import com.netflix.eureka.found.sentinel.ServerContext;
import com.netflix.eureka.found.sentinel.ServerContextHolder;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/paramFlow")
@Produces({"application/xml", "application/json"})
public class ParamFlowRuleResource {

    private final Logger logger = LoggerFactory.getLogger(ParamFlowRuleResource.class);

    private HttpapiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    private RuleRepositoryAdapter<ParamFlowRuleEntity> repository;

    @Inject
    ParamFlowRuleResource(ServerContext serverContext) {
        this.sentinelApiClient = serverContext.getHttpapiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
        this.repository = serverContext.getParamFlowRule();
    }

    public ParamFlowRuleResource() {
        this(ServerContextHolder.getSecurity().getServerContext());
    }
    
    @GET
    public Restresult<List<ParamFlowRuleEntity>> apiQueryRules(@QueryParam("app") String app,
    		@QueryParam("instanceId") String instanceId) {
        try {
        	List<ParamFlowRuleEntity> rules = new ArrayList<ParamFlowRuleEntity>();
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		Collection<ParamFlowRuleEntity> list = repository.getRule(new Resource(RuleType.PARAM_RULE_TYPE, instanceId));
        		if(list == null || list.isEmpty()) {
        			list = sentinelApiClient.fetchParamFlowRulesOfMachine(instanceInfo.getAppName(), instanceInfo.getHomePageUrl()).get();
        			if(list != null) {
                    	rules.addAll(list);
                    }
                    repository.setRule(new Resource(RuleType.PARAM_RULE_TYPE, instanceId), rules);
        		} else {
        			rules.addAll(list);
        		}
        	}
            return Restresult.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying parameter flow rules", throwable);
            return Restresult.ofFailure(-1, throwable.getMessage());
        }
    }

    @Deprecated
    public Restresult<ParamFlowRuleEntity> apiAddRules(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
        ParamFlowRuleEntity entity = new ParamFlowRuleEntity(inEntityInternal(queryParams));
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtCreate(new Date());
        		if(repository.setRule(new Resource(RuleType.PARAM_RULE_TYPE, instanceId), entity)) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
        			if(!status) {
        				logger.warn("Publish degrade rules failed, app={} | {}", app, status);
        			}
        		}
        	}
            return Restresult.ofSuccess(entity);
        } catch (Throwable throwable) {
            logger.error("Error when adding new parameter flow rules", throwable);
            return Restresult.ofFailure(-1, throwable.getMessage());
        }
    }

    @PUT
    public Restresult<ParamFlowRuleEntity> apiUpdateParamFlowRule(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
        try {
        	ParamFlowRuleEntity entity = new ParamFlowRuleEntity(inEntityInternal(queryParams));
        	if(StringUtils.isEmpty(entity.getId())) {
        		return Restresult.ofFailure(-1, "Unable to get the value of id.");
        	}
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtModified(new Date());
        		if(repository.setRule(new Resource(RuleType.PARAM_RULE_TYPE, instanceId), entity)) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
        			if(!status) {
        				logger.warn("Publish degrade rules failed, app={} | {}", app, status);
        			}
        		}
        	}
            return Restresult.ofSuccess(entity);
        } catch (Throwable throwable) {
            logger.error("Error when updating parameter flow rules, app=" + app, throwable);
            return Restresult.ofFailure(-1, throwable.getMessage());
        }
    }

    @DELETE
    public Restresult<String> apiDeleteRule(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		@QueryParam("id") Long id) {
        try {
        	ParamFlowRuleEntity entity = repository.getRule(new Resource(RuleType.PARAM_RULE_TYPE, instanceId), id);
        	if(entity == null) {
        		return Restresult.ofFailure(-1, "Unable to get the value of id.");
        	}
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		boolean remove = repository.removeRule(new Resource(RuleType.PARAM_RULE_TYPE, instanceId), entity);
        		if(remove) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
            		if(!status) {
            			repository.setRule(new Resource(RuleType.FLOW_RULE_TYPE, instanceId), entity);
            			logger.warn("Publish degrade rules failed, app={} | {}", app, status);
            		}
        		}
        	}
            return Restresult.ofSuccess("Ok");
        } catch (Throwable throwable) {
            logger.error("Error when deleting parameter flow rules", throwable);
            return Restresult.ofFailure(-1, throwable.getMessage());
        }
    }
    
    private ParamFlowRule inEntityInternal(MultivaluedMap<String, String> queryParams) {
    	ParamFlowRule entity = new ParamFlowRule();
    	entity.setResource(queryParams.getFirst("resource"));
    	entity.setLimitApp(queryParams.getFirst("limitApp"));
        String burstCount = queryParams.getFirst("burstCount");
        if(StringUtil.isNotEmpty(burstCount)) {
        	entity.setBurstCount(Integer.valueOf(burstCount));
        }
        String clusterMode = queryParams.getFirst("clusterMode");
        if(StringUtil.isNotEmpty(clusterMode)) {
        	entity.setClusterMode(Boolean.valueOf(clusterMode));
        }
        String controlBehavior = queryParams.getFirst("controlBehavior");
        if(StringUtil.isNotEmpty(controlBehavior)) {
        	entity.setControlBehavior(Integer.valueOf(controlBehavior));
        }
        String count = queryParams.getFirst("count");
        if(StringUtil.isNotEmpty(count)) {
        	entity.setCount(Integer.valueOf(count));
        }
        String grade = queryParams.getFirst("grade");
        if(StringUtil.isNotEmpty(grade)) {
        	entity.setGrade(Integer.valueOf(grade));
        }
        String durationInSec = queryParams.getFirst("durationInSec");
        if(StringUtil.isNotEmpty(durationInSec)) {
        	entity.setDurationInSec(Integer.valueOf(durationInSec));
        }
        String maxQueueingTimeMs = queryParams.getFirst("maxQueueingTimeMs");
        if(StringUtil.isNotEmpty(maxQueueingTimeMs)) {
        	entity.setMaxQueueingTimeMs(Integer.valueOf(maxQueueingTimeMs));
        }
        String paramIdx = queryParams.getFirst("paramIdx");
        if(StringUtil.isNotEmpty(paramIdx)) {
        	entity.setParamIdx(Integer.valueOf(paramIdx));
        }
        String clusterConfig = queryParams.getFirst("clusterConfig");
        if(StringUtil.isNotEmpty(clusterConfig)) {
        	entity.setClusterConfig(JSONFormatter.fromJSON(clusterConfig, ParamFlowClusterConfig.class));
        }
        String paramFlowItemList = queryParams.getFirst("paramFlowItemList");
        if(StringUtil.isNotEmpty(paramFlowItemList)) {
        	entity.setParamFlowItemList(JSONFormatter.fromList(paramFlowItemList, ParamFlowItem.class));
        }
        return entity;
    }
    
    private boolean publishRules(String instanceId, String homePage) {
        Collection<ParamFlowRuleEntity> rules = repository.getRule(new Resource(RuleType.PARAM_RULE_TYPE, instanceId));
        return sentinelApiClient.setParamRuleOfMachine(homePage, rules);
    }

}
