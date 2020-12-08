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

import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.netflix.eureka.dashboard.repository.rule.RuleRepository;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/system")
@Produces({"application/xml", "application/json"})
public class SystemResource {
    private final Logger logger = LoggerFactory.getLogger(SystemResource.class);
    
    private SentinelApiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    private RuleRepository<SystemRuleEntity> repository;

    @Inject
    SystemResource(SentinelServerContext serverContext) {
        this.sentinelApiClient = serverContext.getSentinelApiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
        this.repository = serverContext.getSystemRule();
    }

    public SystemResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }
    
    @GET
    public Restresult<List<SystemRuleEntity>> apiRules(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	List<SystemRuleEntity> rules = new ArrayList<SystemRuleEntity>();
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		List<SystemRuleEntity> list = sentinelApiClient.fetchSystemRuleOfMachine(app, instanceInfo.getHomePageUrl());
        		if(list != null) {
        			rules.addAll(list);
            	}
        		repository.saveAll(rules);
        	}
            return new Restresult<>(rules);
        } catch (Throwable throwable) {
            logger.error("Query machine system rules error", throwable);
            return errorResponse(throwable);
        }
    }

    @PUT
    public Restresult<SystemRuleEntity> apiNew(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
    	SystemRuleEntity entity = inEntityInternal(queryParams);
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtCreate(new Date());
        		repository.save(entity);
        		boolean status = publishRules(entity.getApp(), instanceInfo.getHomePageUrl());
        		logger.warn("Publish system rules failed, app={} | {}", entity.getApp(), status);
        	}
        } catch (Throwable throwable) {
            logger.error("Add SystemRule error", throwable);
            return errorResponse(throwable);
        }
        
        return new Restresult<>(entity);
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
        		repository.save(entity);
        		boolean status = publishRules(entity.getApp(), instanceInfo.getHomePageUrl());
        		logger.warn("Publish system rules failed, app={} | {}", entity.getApp(), status);
        	}
        } catch (Throwable throwable) {
            logger.error("save error:", throwable);
            return errorResponse(throwable);
        }
        
        return new Restresult<>(entity);
    }

    @DELETE
    @Path("{id}")
    public Restresult<Long> apiDelete(@PathParam("id") Long id, @QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	SystemRuleEntity oldEntity = repository.delete(app, id);
            if (oldEntity == null) {
                return new Restresult<>(null);
            }
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		if (!publishRules(oldEntity.getApp(), instanceInfo.getHomePageUrl())) {
                    logger.info("publish system rules fail after rule delete");
                }
        	}
        } catch (Throwable throwable) {
            logger.error("delete error:", throwable);
            return errorResponse(throwable);
        }
        
        return new Restresult<>(id);
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
        return new Restresult<>(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }
    
    private boolean publishRules(String app, String homePage) {
        List<SystemRuleEntity> rules = repository.findAllByApp(app);
        return sentinelApiClient.setSystemRuleOfMachine(app, homePage, rules);
    }
}
