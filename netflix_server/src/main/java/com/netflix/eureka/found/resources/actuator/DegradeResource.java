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
import com.netflix.eureka.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.repository.RuleRepositoryAdapter;
import com.netflix.eureka.found.sentinel.ServerContext;
import com.netflix.eureka.found.sentinel.ServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/degrade")
@Produces({"application/xml", "application/json"})
public class DegradeResource {

    private final Logger logger = LoggerFactory.getLogger(DegradeResource.class);

    private HttpapiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    private RuleRepositoryAdapter<DegradeRuleEntity> repository;

    @Inject
    DegradeResource(ServerContext serverContext) {
    	this.sentinelApiClient = serverContext.getHttpapiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
        this.repository = serverContext.getDegradeRule();
    }

    public DegradeResource() {
        this(ServerContextHolder.getSecurity().getServerContext());
    }
    
    @GET
    public Restresult<List<DegradeRuleEntity>> apiQueryRules(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	List<DegradeRuleEntity> rules = new ArrayList<DegradeRuleEntity>();
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		Collection<DegradeRuleEntity> list = repository.getRule(new Resource(RuleType.DEGRADE_RULE_TYPE, instanceId));
        		if(list == null || list.isEmpty()) {
        			list = sentinelApiClient.fetchDegradeRuleOfMachine(instanceInfo.getAppName(), instanceInfo.getHomePageUrl());
        			if(list != null) {
                    	rules.addAll(list);
                    }
                    repository.setRule(new Resource(RuleType.DEGRADE_RULE_TYPE, instanceId), rules);
        		} else {
        			rules.addAll(list);
        		}
        	}
            return Restresult.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("queryApps error:", throwable);
            return errorResponse(throwable);
        }
    }

    @POST
    public Restresult<DegradeRuleEntity> apiAddRule(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
    	DegradeRuleEntity entity = inEntityInternal(queryParams);
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtCreate(new Date());
        		if(repository.setRule(new Resource(RuleType.DEGRADE_RULE_TYPE, instanceId), entity)) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
            		if(!status) {
            			logger.warn("Publish degrade rules failed, app={} | {}", app, status);
            		}
        		}
        	}
        } catch (Throwable t) {
            logger.error("Failed to add new degrade rule, app={}", entity.getApp(), t);
            return errorResponse(t);
        }
        
        return Restresult.ofSuccess(entity);
    }

    @PUT
    public Restresult<DegradeRuleEntity> apiUpdateRule(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
        try {
        	DegradeRuleEntity entity = inEntityInternal(queryParams);
        	if(StringUtils.isEmpty(entity.getId())) {
        		return Restresult.ofFailure(-1, "Unable to get the value of id.");
        	}
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtModified(new Date());
        		if(repository.setRule(new Resource(RuleType.DEGRADE_RULE_TYPE, instanceId), entity)) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
        			if(!status) {
        				logger.warn("Publish degrade rules failed, app={} | {}", app, status);
        			}
        		}
        	}
        	return Restresult.ofSuccess(entity);
        } catch (Throwable t) {
            logger.error("Failed to save degrade rule, app={}", app, t);
            return errorResponse(t);
        }
    }

    @DELETE
    public Restresult<String> delete(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		@QueryParam("id") Long id) {
        try {
        	DegradeRuleEntity entity = repository.getRule(new Resource(RuleType.DEGRADE_RULE_TYPE, instanceId), id);
        	if(entity == null) {
        		return Restresult.ofFailure(-1, "Unable to get the value of id.");
        	}
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		boolean remove = repository.removeRule(new Resource(RuleType.DEGRADE_RULE_TYPE, instanceId), entity);
        		if(remove) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
            		if(!status) {
            			repository.setRule(new Resource(RuleType.DEGRADE_RULE_TYPE, instanceId), entity);
            			logger.warn("Publish degrade rules failed, app={} | {}", app, status);
            		}
        		}
        	}
        } catch (Throwable throwable) {
            logger.error("Failed to delete degrade rule, app={}", app, throwable);
            return errorResponse(throwable);
        }
        
        return Restresult.ofSuccess("Ok");
    }

    private <T> Restresult<T> errorResponse(Throwable ex) {
        return Restresult.ofFailure(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }
    
    private boolean publishRules(String instanceId, String homePage) {
        Collection<DegradeRuleEntity> rules = repository.getRule(new Resource(RuleType.DEGRADE_RULE_TYPE, instanceId));
        return sentinelApiClient.setDegradeRuleOfMachine(homePage, rules);
    }

    private DegradeRuleEntity inEntityInternal(MultivaluedMap<String, String> queryParams) {
    	DegradeRuleEntity entity = new DegradeRuleEntity();
    	entity.setApp(queryParams.getFirst("app"));
    	entity.setInstanceId(queryParams.getFirst("instanceId"));
    	entity.setLimitApp(queryParams.getFirst("limitApp"));
    	entity.setResource(queryParams.getFirst("resource"));
    	String threshold = queryParams.getFirst("count");
        if(StringUtil.isNotEmpty(threshold)) {
        	entity.setCount(Double.valueOf(threshold));
        }
        String recoveryTimeoutSec = queryParams.getFirst("timeWindow");
        if (StringUtil.isNotEmpty(recoveryTimeoutSec)) {
        	entity.setTimeWindow(Integer.valueOf(recoveryTimeoutSec));
        }
        String strategy = queryParams.getFirst("grade");
        if (StringUtil.isNotEmpty(strategy)) {
        	entity.setGrade(Integer.valueOf(strategy));
        }
        String minRequestAmount = queryParams.getFirst("minRequestAmount");
        if (StringUtil.isNotEmpty(minRequestAmount)) {
        	entity.setMinRequestAmount(Integer.valueOf(minRequestAmount));
        }
        String statIntervalMs = queryParams.getFirst("statIntervalMs");
        if (StringUtil.isNotEmpty(statIntervalMs)) {
        	entity.setStatIntervalMs(Integer.valueOf(statIntervalMs));
        }
        if ("0".equals(strategy)) {
            String slowRatio = queryParams.getFirst("slowRatioThreshold");
            if (StringUtil.isNotEmpty(slowRatio)) {
            	entity.setSlowRatioThreshold(Double.valueOf(slowRatio));
            }
        }
        return entity;
    }
}
