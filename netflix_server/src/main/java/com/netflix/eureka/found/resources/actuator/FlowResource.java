package com.netflix.eureka.found.resources.actuator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.command.Resource;
import com.netflix.eureka.command.Resource.RuleType;
import com.netflix.eureka.dashboard.client.HttpapiClient;
import com.netflix.eureka.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.repository.RuleRepositoryAdapter;
import com.netflix.eureka.found.sentinel.ServerContext;
import com.netflix.eureka.found.sentinel.ServerContextHolder;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/flow")
@Produces({"application/xml", "application/json"})
public class FlowResource {

    private final Logger logger = LoggerFactory.getLogger(FlowResource.class);

    private HttpapiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    
    private RuleRepositoryAdapter<FlowRuleEntity> repository;
    
    @Inject
    FlowResource(ServerContext serverContext) {
    	this.sentinelApiClient = serverContext.getHttpapiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
        this.repository = serverContext.getFlowRule();
    }

    public FlowResource() {
        this(ServerContextHolder.getSecurity().getServerContext());
    }

    @GET
    public Restresult<List<FlowRuleEntity>> apiQueryRules(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	List<FlowRuleEntity> rules = new ArrayList<FlowRuleEntity>();
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		Collection<FlowRuleEntity> list = repository.getRule(new Resource(RuleType.FLOW_RULE_TYPE, instanceId));
        		if(list == null || list.isEmpty()) {
        			list = sentinelApiClient.fetchFlowRuleOfMachine(instanceInfo.getAppName(), instanceInfo.getHomePageUrl());
        			if(list != null) {
                    	rules.addAll(list);
                    }
                    repository.setRule(new Resource(RuleType.FLOW_RULE_TYPE, instanceId), rules);
        		} else {
        			rules.addAll(list);
        		}
        	}
            return Restresult.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying flow rules", throwable);
            return errorResponse(throwable);
        }
    }

    @POST
    public Restresult<FlowRuleEntity> apiAddFlowRule(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
    	FlowRuleEntity entity = inEntityInternal(queryParams);
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtCreate(new Date());
        		if(repository.setRule(new Resource(RuleType.FLOW_RULE_TYPE, instanceId), entity)) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
            		if(!status) {
            			logger.warn("Publish degrade rules failed, app={} | {}", app, status);
            		}
        		}
        		
        	}
        } catch (Throwable t) {
            logger.error("Failed to save degrade rule, rule={}", entity, t);
            return errorResponse(t);
        }
        return Restresult.ofSuccess(entity);
    }

    @PUT
    public Restresult<FlowRuleEntity> apiUpdateFlowRule(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
    	FlowRuleEntity entity = inEntityInternal(queryParams);
    	if(StringUtils.isEmpty(entity.getId())) {
    		return Restresult.ofFailure(-1, "Unable to get the value of id.");
    	}
    	
    	Integer strategy = entity.getStrategy();
    	if (strategy != null && strategy > 0) {
            if (StringUtil.isBlank(entity.getRefResource())) {
                return Restresult.ofFailure(-1, "refResource can't be null or empty when strategy!=0");
            }
        }
    	
    	Integer controlBehavior = entity.getControlBehavior();
        if (controlBehavior != null) {
            if (controlBehavior != 0 && controlBehavior != 1 && controlBehavior != 2) {
                return Restresult.ofFailure(-1, "controlBehavior must be in [0, 1, 2], but " + controlBehavior + " got");
            }
            if (controlBehavior == 1 && entity.getWarmUpPeriodSec() == null) {
                return Restresult.ofFailure(-1, "warmUpPeriodSec can't be null when controlBehavior==1");
            }
            if (controlBehavior == 2 && entity.getMaxQueueingTimeMs() == null) {
                return Restresult.ofFailure(-1, "maxQueueingTimeMs can't be null when controlBehavior==2");
            }
        }
        
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtModified(new Date());
        		if(repository.setRule(new Resource(RuleType.FLOW_RULE_TYPE, instanceId), entity)) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
        			if(!status) {
        				logger.warn("Publish degrade rules failed, app={} | {}", app, status);
        			}
        		}
        	}
        } catch (Throwable t) {
            logger.error("Failed to save degrade rule, rule={}", entity, t);
            return errorResponse(t);
        }
        return Restresult.ofSuccess(entity);
    }

    @DELETE
    public Restresult<String> apiDeleteFlowRule(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		@QueryParam("id") Long id) {
        try {
        	FlowRuleEntity entity = repository.getRule(new Resource(RuleType.FLOW_RULE_TYPE, instanceId), id);
        	if(entity == null) {
        		return Restresult.ofFailure(-1, "Unable to get the value of id.");
        	}
        	
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		boolean remove = repository.removeRule(new Resource(RuleType.FLOW_RULE_TYPE, instanceId), entity);
        		if(remove) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
            		if(!status) {
            			repository.setRule(new Resource(RuleType.FLOW_RULE_TYPE, instanceId), entity);
            			logger.warn("Publish degrade rules failed, app={} | {}", app, status);
            		}
        		}
        	}
        } catch (Throwable e) {
            return Restresult.ofFailure(-1, e.getMessage());
        }
        return Restresult.ofSuccess("Ok");
    }

    private FlowRuleEntity inEntityInternal(MultivaluedMap<String, String> queryParams) {
    	FlowRuleEntity entity = new FlowRuleEntity();
        entity.setLimitApp(queryParams.getFirst("limitApp"));
        entity.setResource(queryParams.getFirst("resource"));
        String grade = queryParams.getFirst("grade");
        if(StringUtil.isNotEmpty(grade)) {
        	entity.setGrade(Integer.valueOf(grade));
        }
        String count = queryParams.getFirst("count");
        if(StringUtil.isNotEmpty(count)) {
        	entity.setCount(Double.valueOf(count));
        }
        String strategy = queryParams.getFirst("strategy");
        if(StringUtil.isNotEmpty(strategy)) {
        	entity.setStrategy(Integer.valueOf(strategy));
        }
        entity.setRefResource(queryParams.getFirst("refResource"));
        String controlBehavior = queryParams.getFirst("controlBehavior");
        if(StringUtil.isNotEmpty(controlBehavior)) {
        	entity.setControlBehavior(Integer.valueOf(controlBehavior));
        }
        String warmUpPeriodSec = queryParams.getFirst("warmUpPeriodSec");
        if(StringUtil.isNotEmpty(warmUpPeriodSec)) {
        	entity.setWarmUpPeriodSec(Integer.valueOf(warmUpPeriodSec));
        }
        String maxQueueingTimeMs = queryParams.getFirst("maxQueueingTimeMs");
        if(StringUtil.isNotEmpty(maxQueueingTimeMs)) {
        	entity.setMaxQueueingTimeMs(Integer.valueOf(maxQueueingTimeMs));
        }
        String clusterMode = queryParams.getFirst("clusterMode");
        if(StringUtil.isNotEmpty(clusterMode)) {
        	entity.setClusterMode(Boolean.valueOf(clusterMode));
        }
        String clusterConfig = queryParams.getFirst("clusterConfig");
        if(StringUtil.isNotEmpty(clusterConfig)) {
        	entity.setClusterConfig(JSONFormatter.fromJSON(clusterConfig, ClusterFlowConfig.class));
        }
        return entity;
    }
    
    private <T> Restresult<T> errorResponse(Throwable ex) {
        return Restresult.ofFailure(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }
    
    private boolean publishRules(String instanceId, String homePage) {
    	Collection<FlowRuleEntity> rules = repository.getRule(new Resource(RuleType.FLOW_RULE_TYPE, instanceId));
        return sentinelApiClient.setFlowRuleOfMachine(homePage, rules);
    }
}
