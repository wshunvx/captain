package com.netflix.eureka.found.resources.actuator;

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

import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.netflix.eureka.dashboard.repository.rule.RuleRepository;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/degrade")
@Produces({"application/xml", "application/json"})
public class DegradeResource {

    private final Logger logger = LoggerFactory.getLogger(DegradeResource.class);

    private SentinelApiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    private RuleRepository<DegradeRuleEntity> repository;

    @Inject
    DegradeResource(SentinelServerContext serverContext) {
    	this.sentinelApiClient = serverContext.getSentinelApiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
        this.repository = serverContext.getDegradeRule();
    }

    public DegradeResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }
    
    @GET
    public Restresult<List<DegradeRuleEntity>> apiQueryRules(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	List<DegradeRuleEntity> rules = null;
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		rules = sentinelApiClient.fetchDegradeRuleOfMachine(app, instanceInfo.getHomePageUrl());
        	}
        	if(rules != null) {
        		repository.saveAll(rules);
        	}
            return new Restresult<>(rules);
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
        		repository.save(entity);
        		boolean status = publishRules(entity.getApp(), instanceInfo.getHomePageUrl());
                logger.warn("Publish degrade rules failed, app={} | {}", entity.getApp(), status);
        	}
        } catch (Throwable t) {
            logger.error("Failed to add new degrade rule, app={}, id={}", entity.getApp(), entity.getId(), t);
            return errorResponse(t);
        }
        
        return new Restresult<>(entity);
    }

    @PUT
    public Restresult<DegradeRuleEntity> apiUpdateRule(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
        DegradeRuleEntity entity = inEntityInternal(queryParams);
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
            logger.error("Failed to save degrade rule, id={}, rule={}", entity.getId(), entity, t);
            return errorResponse(t);
        }
        return new Restresult<>(entity);
    }

    @DELETE
    @Path("{id}")
    public Restresult<Long> delete(@PathParam("id") Long id, 
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	DegradeRuleEntity oldEntity = repository.delete(app, id);
            if (oldEntity == null) {
                return new Restresult<>(null);
            }
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		if (!publishRules(oldEntity.getApp(), instanceInfo.getHomePageUrl())) {
                    logger.warn("Publish degrade rules failed, app={}", oldEntity.getApp());
                }
        	}
        } catch (Throwable throwable) {
            logger.error("Failed to delete degrade rule, id={}", id, throwable);
            return errorResponse(throwable);
        }
        
        return new Restresult<>(id);
    }

    private <T> Restresult<T> errorResponse(Throwable ex) {
        return new Restresult<>(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }
    
    private boolean publishRules(String app, String homePage) {
        List<DegradeRuleEntity> rules = repository.findAllByApp(app);
        return sentinelApiClient.setDegradeRuleOfMachine(app, homePage, rules);
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
