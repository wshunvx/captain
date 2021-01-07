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

import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.command.Resource;
import com.netflix.eureka.command.Resource.RuleType;
import com.netflix.eureka.dashboard.client.HttpapiClient;
import com.netflix.eureka.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.repository.RuleRepositoryAdapter;
import com.netflix.eureka.found.sentinel.ServerContext;
import com.netflix.eureka.found.sentinel.ServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/system")
@Produces({"application/xml", "application/json"})
public class SystemResource {
    private final Logger logger = LoggerFactory.getLogger(SystemResource.class);
    
    private HttpapiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    private RuleRepositoryAdapter<SystemRuleEntity> repository;

    @Inject
    SystemResource(ServerContext serverContext) {
        this.sentinelApiClient = serverContext.getHttpapiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
        this.repository = serverContext.getSystemRule();
    }

    public SystemResource() {
        this(ServerContextHolder.getSecurity().getServerContext());
    }
    
    @GET
    public Restresult<List<SystemRuleEntity>> apiRules(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	List<SystemRuleEntity> rules = new ArrayList<SystemRuleEntity>();
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		Collection<SystemRuleEntity> list = repository.getRule(new Resource(RuleType.SYSTEM_RULE_TYPE, instanceId));
        		if(list == null || list.isEmpty()) {
        			list = sentinelApiClient.fetchSystemRuleOfMachine(instanceInfo.getAppName(), instanceInfo.getHomePageUrl());
        			if(list != null) {
                    	rules.addAll(list);
                    }
                    repository.setRule(new Resource(RuleType.SYSTEM_RULE_TYPE, instanceId), rules);
        		} else {
        			rules.addAll(list);
        		}
        	}
            return Restresult.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Query machine system rules error", throwable);
            return errorResponse(throwable);
        }
    }

    @POST
    public Restresult<SystemRuleEntity> apiSave(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
    	SystemRuleEntity entity = inEntityInternal(queryParams);
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtModified(new Date());
        		if(repository.setRule(new Resource(RuleType.SYSTEM_RULE_TYPE, instanceId), entity)) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
            		if(!status) {
            			logger.warn("Publish degrade rules failed, app={} | {}", app, status);
            		}
        		}
        	}
        } catch (Throwable throwable) {
            logger.error("save error:", throwable);
            return errorResponse(throwable);
        }
        
        return Restresult.ofSuccess(entity);
    }
    
    @PUT
    public Restresult<SystemRuleEntity> apiUpdate(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
        try {
        	SystemRuleEntity entity = inEntityInternal(queryParams);
        	if(StringUtils.isEmpty(entity.getId())) {
        		return Restresult.ofFailure(-1, "Unable to get the value of id.");
        	}
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtCreate(new Date());
        		if(repository.setRule(new Resource(RuleType.SYSTEM_RULE_TYPE, instanceId), entity)) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
        			if(!status) {
        				logger.warn("Publish degrade rules failed, app={} | {}", app, status);
        			}
        		}
        	}
        	return Restresult.ofSuccess(entity);
        } catch (Throwable throwable) {
            logger.error("Add SystemRule error", throwable);
            return errorResponse(throwable);
        }
    }

    @DELETE
    public Restresult<String> apiDelete(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		@QueryParam("id") Long id) {
        try {
        	SystemRuleEntity entity = repository.getRule(new Resource(RuleType.SYSTEM_RULE_TYPE, instanceId), id);
        	if(entity == null) {
        		return Restresult.ofFailure(-1, "Unable to get the value of id.");
        	}
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		boolean remove = repository.removeRule(new Resource(RuleType.SYSTEM_RULE_TYPE, instanceId), entity);
        		if(remove) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
            		if(!status) {
            			repository.setRule(new Resource(RuleType.SYSTEM_RULE_TYPE, instanceId), entity);
            			logger.warn("Publish degrade rules failed, app={} | {}", app, status);
            		}
        		}
        	}
        } catch (Throwable throwable) {
            logger.error("delete error:", throwable);
            return errorResponse(throwable);
        }
        
        return Restresult.ofSuccess("Ok");
    }

    private SystemRuleEntity inEntityInternal(MultivaluedMap<String, String> queryParams) {
    	SystemRuleEntity entity = new SystemRuleEntity();
        entity.setApp(queryParams.getFirst("app"));
        entity.setInstanceId(queryParams.getFirst("instanceId"));
        String avgRt = queryParams.getFirst("avgRt");
        if(StringUtil.isNotEmpty(avgRt)) {
        	entity.setAvgRt(Long.valueOf(avgRt));
        }
        String highestCpuUsage = queryParams.getFirst("highestCpuUsage");
        if(StringUtil.isNotEmpty(highestCpuUsage)) {
        	entity.setHighestCpuUsage(Double.valueOf(highestCpuUsage));
        }
        String highestSystemLoad = queryParams.getFirst("highestSystemLoad");
        if(StringUtil.isNotEmpty(highestSystemLoad)) {
        	entity.setHighestSystemLoad(Double.valueOf(highestSystemLoad));
        }
        String maxThread = queryParams.getFirst("maxThread");
        if(StringUtil.isNotEmpty(maxThread)) {
        	entity.setMaxThread(Long.valueOf(maxThread));
        }
        String qps = queryParams.getFirst("qps");
        if(StringUtil.isNotEmpty(qps)) {
        	entity.setQps(Double.valueOf(qps));
        }
        return entity;
    }
    
    private <T> Restresult<T> errorResponse(Throwable ex) {
        return Restresult.ofFailure(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }
    
    private boolean publishRules(String instanceId, String homePage) {
        Collection<SystemRuleEntity> rules = repository.getRule(new Resource(RuleType.SYSTEM_RULE_TYPE, instanceId));
        return sentinelApiClient.setSystemRuleOfMachine(homePage, rules);
    }
}
