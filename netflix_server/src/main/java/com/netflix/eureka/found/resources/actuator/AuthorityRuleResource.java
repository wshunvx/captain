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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.netflix.eureka.dashboard.repository.rule.RuleRepository;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/authority")
@Produces({"application/xml", "application/json"})
public class AuthorityRuleResource {

    private final Logger logger = LoggerFactory.getLogger(AuthorityRuleResource.class);

    private SentinelApiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    private RuleRepository<AuthorityRuleEntity> repository;

    @Inject
    AuthorityRuleResource(SentinelServerContext serverContext) {
    	this.sentinelApiClient = serverContext.getSentinelApiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
        this.repository = serverContext.getAuthorityRule();
    }

    public AuthorityRuleResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }
    
    @GET
    public Restresult<List<AuthorityRuleEntity>> apiQueryAllRulesForMachine(
    		@QueryParam("app") String app, @QueryParam("id") String id) {
        if (StringUtil.isEmpty(app)) {
            return new Restresult<>(-1, "app cannot be null or empty");
        }
        if (StringUtil.isEmpty(id)) {
            return new Restresult<>(-1, "id cannot be null or empty");
        }
        try {
        	InstanceInfo instanceInfo = null;
        	Application application = instanceRegistry.getApplication(app);
        	if(application != null) {
        		instanceInfo = application.getByInstanceId(id);
        	}
        	
        	List<AuthorityRuleEntity> rules = null;
        	if(instanceInfo != null) {
        		rules = sentinelApiClient.fetchAuthorityRulesOfMachine(app, instanceInfo.getHomePageUrl());
        	}
            
        	if(rules != null) {
        		repository.saveAll(rules);
        	}
        	
            return new Restresult<>(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying authority rules", throwable);
            return new Restresult<>(-1, throwable.getMessage());
        }
    }

    @POST
    public Restresult<AuthorityRuleEntity> apiAddAuthorityRule(AuthorityRuleEntity entity) {
    	Restresult<AuthorityRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(entity.getApp(), entity.getInstanceId());
        	if(instanceInfo != null) {
        		if (!publishRules(entity.getApp(), instanceInfo.getHomePageUrl())) {
        			logger.info("Publish authority rules failed after rule add");
                }
        		
        		repository.save(entity);
        	}
        } catch (Throwable throwable) {
            logger.error("Failed to add authority rule", throwable);
            return errorResponse(throwable);
        }
        return new Restresult<>(entity);
    }

    @PUT
    @Path("{id}")
    public Restresult<AuthorityRuleEntity> apiUpdateParamFlowRule(@PathParam("id") Long id, AuthorityRuleEntity entity) {
        Restresult<AuthorityRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        entity.setId(id);
        Date date = new Date();
        entity.setGmtCreate(null);
        entity.setGmtModified(date);
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(entity.getApp(), entity.getInstanceId());
        	if(instanceInfo != null) {
        		if (!publishRules(entity.getApp(), instanceInfo.getHomePageUrl())) {
        			logger.info("Publish authority rules failed after rule update");
                }
        		
        		repository.save(entity);
        	}
        } catch (Throwable throwable) {
            logger.error("Failed to save authority rule", throwable);
            return errorResponse(throwable);
        }
        return new Restresult<>(entity);
    }

    @DELETE
    @Path("{id}")
    public Restresult<Long> apiDeleteRule(@PathParam("id") Long id, @QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        if (id == null) {
            return new Restresult<>(-1, "id cannot be null");
        }
        AuthorityRuleEntity oldEntity = repository.delete(app, id);
        if (oldEntity == null) {
            return new Restresult<>(null);
        }
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		if (!publishRules(oldEntity.getApp(), instanceInfo.getHomePageUrl())) {
        			logger.info("Publish authority rules failed after rule update");
                }
        		
        	}
        } catch (Exception e) {
            return new Restresult<>(-1, e.getMessage());
        }
        return new Restresult<>(id);
    }

    private <R> Restresult<R> checkEntityInternal(AuthorityRuleEntity entity) {
        if (entity == null) {
            return new Restresult<>(-1, "bad rule body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return new Restresult<>(-1, "app can't be null or empty");
        }
        if (entity.getRule() == null) {
            return new Restresult<>(-1, "rule can't be null");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return new Restresult<>(-1, "resource name cannot be null or empty");
        }
        if (StringUtil.isBlank(entity.getLimitApp())) {
            return new Restresult<>(-1, "limitApp should be valid");
        }
        if (entity.getStrategy() != RuleConstant.AUTHORITY_WHITE
            && entity.getStrategy() != RuleConstant.AUTHORITY_BLACK) {
            return new Restresult<>(-1, "Unknown strategy (must be blacklist or whitelist)");
        }
        return null;
    }
    
    private <T> Restresult<T> errorResponse(Throwable ex) {
        return new Restresult<>(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }
    
    private boolean publishRules(String app, String homePage) {
        List<AuthorityRuleEntity> rules = repository.findAllByApp(app);
        return sentinelApiClient.setAuthorityRuleOfMachine(app, homePage, rules);
    }
}
