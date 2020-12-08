package com.netflix.eureka.found.resources.actuator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.repository.InMemoryRuleRepositoryAdapter;
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/flow")
@Produces({"application/xml", "application/json"})
public class FlowResource {

    private final Logger logger = LoggerFactory.getLogger(FlowResource.class);

    private InMemoryRuleRepositoryAdapter<FlowRuleEntity> repository;

    private SentinelApiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    
    @Inject
    FlowResource(SentinelServerContext serverContext) {
    	this.sentinelApiClient = serverContext.getSentinelApiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
        this.repository = serverContext.getInMemoryRule();
    }

    public FlowResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }

    @GET
    public Restresult<List<FlowRuleEntity>> apiQueryRules(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	List<FlowRuleEntity> rules = new ArrayList<FlowRuleEntity>();
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		List<FlowRuleEntity> list = sentinelApiClient.fetchFlowRuleOfMachine(instanceInfo.getAppName(), instanceInfo.getHomePageUrl());
                if(list != null) {
                	rules.addAll(list);
                }
                repository.saveAll(rules);
        	}
            return new Restresult<>(rules);
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
        		repository.save(entity);
        		boolean status = publishRules(entity.getApp(), instanceInfo.getHomePageUrl());
        		logger.warn("Publish degrade rules failed, app={} | {}", entity.getApp(), status);
        	}
        } catch (Throwable t) {
            logger.error("Failed to save degrade rule, rule={}", entity, t);
            return errorResponse(t);
        }
        return new Restresult<>(entity);
    }

    @PUT
    public Restresult<FlowRuleEntity> apiUpdateFlowRule(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
    	FlowRuleEntity entity = inEntityInternal(queryParams);
    	Integer strategy = entity.getStrategy();
    	if (strategy != null && strategy > 0) {
            if (StringUtil.isBlank(entity.getRefResource())) {
                return new Restresult<>(-1, "refResource can't be null or empty when strategy!=0");
            }
        }
    	
    	Integer controlBehavior = entity.getControlBehavior();
        if (controlBehavior != null) {
            if (controlBehavior != 0 && controlBehavior != 1 && controlBehavior != 2) {
                return new Restresult<>(-1, "controlBehavior must be in [0, 1, 2], but " + controlBehavior + " got");
            }
            if (controlBehavior == 1 && entity.getWarmUpPeriodSec() == null) {
                return new Restresult<>(-1, "warmUpPeriodSec can't be null when controlBehavior==1");
            }
            if (controlBehavior == 2 && entity.getMaxQueueingTimeMs() == null) {
                return new Restresult<>(-1, "maxQueueingTimeMs can't be null when controlBehavior==2");
            }
        }
        
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtModified(new Date());
        		repository.save(entity);
        		boolean status = publishRules(entity.getApp(), instanceInfo.getHomePageUrl());
        		logger.warn("Publish degrade rules failed, app={} | {}", entity.getApp(), status);
        	}
        } catch (Throwable t) {
            logger.error("Failed to save degrade rule, rule={}", entity, t);
            return errorResponse(t);
        }
        return new Restresult<>(entity);
    }

    @DELETE
    @Path("{id}")
    public Restresult<Long> apiDeleteFlowRule(@PathParam("id") Long id, @QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	FlowRuleEntity oldEntity = repository.delete(app, id);
            if (oldEntity == null) {
                return new Restresult<>(null);
            }
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		publishRules(oldEntity.getApp(), instanceInfo.getHomePageUrl());
        	}
        } catch (Exception e) {
            return new Restresult<>(-1, e.getMessage());
        }
        return new Restresult<>(id);
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
        return new Restresult<>(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }
    
    private boolean publishRules(String app, String homePage) {
        List<FlowRuleEntity> rules = repository.findAllByApp(app);
        return sentinelApiClient.setFlowRuleOfMachine(app, homePage, rules);
    }
}
