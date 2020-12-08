package com.netflix.eureka.found.resources.actuator;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import com.netflix.eureka.common.ParamFlowClusterConfig;
import com.netflix.eureka.common.ParamFlowItem;
import com.netflix.eureka.common.ParamFlowRule;
import com.netflix.eureka.dashboard.client.CommandNotFoundException;
import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.netflix.eureka.dashboard.repository.rule.RuleRepository;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/paramFlow")
@Produces({"application/xml", "application/json"})
public class ParamFlowRuleResource {

    private final Logger logger = LoggerFactory.getLogger(ParamFlowRuleResource.class);

    private SentinelApiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    private RuleRepository<ParamFlowRuleEntity> repository;

    @Inject
    ParamFlowRuleResource(SentinelServerContext serverContext) {
        this.sentinelApiClient = serverContext.getSentinelApiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
        this.repository = serverContext.getParamFlowRule();
    }

    public ParamFlowRuleResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }
    
    @GET
    public Restresult<List<ParamFlowRuleEntity>> apiQueryRules(@QueryParam("app") String app,
    		@QueryParam("instanceId") String instanceId) {
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		return sentinelApiClient.fetchParamFlowRulesOfMachine(app, instanceInfo.getHomePageUrl())
        				.thenApply(e -> {
        					repository.saveAll(e);
        					return new Restresult<>(e);
        				})
        				.get();
        	}
            return new Restresult<List<ParamFlowRuleEntity>>();
        } catch (ExecutionException ex) {
            logger.error("Error when querying parameter flow rules", ex.getCause());
            if (isNotSupported(ex.getCause())) {
                return unsupportedVersion();
            } else {
                return errorResponse(ex.getCause());
            }
        } catch (Throwable throwable) {
            logger.error("Error when querying parameter flow rules", throwable);
            return new Restresult<>(-1, throwable.getMessage());
        }
    }

    private boolean isNotSupported(Throwable ex) {
        return ex instanceof CommandNotFoundException;
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
        		repository.save(entity);
        		publishRules(entity.getApp(), instanceInfo.getHomePageUrl()).get();
        	}
            return new Restresult<>(entity);
        } catch (ExecutionException ex) {
            logger.error("Error when adding new parameter flow rules", ex.getCause());
            if (isNotSupported(ex.getCause())) {
                return unsupportedVersion();
            } else {
                return errorResponse(ex.getCause());
            }
        } catch (Throwable throwable) {
            logger.error("Error when adding new parameter flow rules", throwable);
            return new Restresult<>(-1, throwable.getMessage());
        }
    }

    @PUT
    public Restresult<ParamFlowRuleEntity> apiUpdateParamFlowRule(
    		@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, 
    		MultivaluedMap<String, String> queryParams) {
    	ParamFlowRuleEntity entity = new ParamFlowRuleEntity(inEntityInternal(queryParams));
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtModified(new Date());
        		repository.save(entity);
        		publishRules(entity.getApp(), instanceInfo.getHomePageUrl()).get();
        		
        	}
            return new Restresult<>(entity);
        } catch (ExecutionException ex) {
            logger.error("Error when updating parameter flow rules, id=" + entity.getId(), ex.getCause());
            if (isNotSupported(ex.getCause())) {
                return unsupportedVersion();
            } else {
                return errorResponse(ex.getCause());
            }
        } catch (Throwable throwable) {
            logger.error("Error when updating parameter flow rules, id=" + entity.getId(), throwable);
            return new Restresult<>(-1, throwable.getMessage());
        }
    }

    @DELETE
    @Path("{id}")
    public Restresult<Long> apiDeleteRule(@PathParam("id") Long id, @QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	ParamFlowRuleEntity oldEntity = repository.delete(app, id);
            if (oldEntity == null) {
                return new Restresult<>(null);
            }
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		publishRules(oldEntity.getApp(), instanceInfo.getHomePageUrl()).get();
        	}
            return new Restresult<>(id);
        } catch (ExecutionException ex) {
            logger.error("Error when deleting parameter flow rules", ex.getCause());
            if (isNotSupported(ex.getCause())) {
                return unsupportedVersion();
            } else {
                return errorResponse(ex.getCause());
            }
        } catch (Throwable throwable) {
            logger.error("Error when deleting parameter flow rules", throwable);
            return new Restresult<>(-1, throwable.getMessage());
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
    
    private <T> Restresult<T> errorResponse(Throwable ex) {
        return new Restresult<>(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }

    private CompletableFuture<Void> publishRules(String app, String homePage) {
        List<ParamFlowRuleEntity> rules = repository.findAllByApp(app);
        return sentinelApiClient.setParamFlowRuleOfMachine(app, homePage, rules);
    }

    private <R> Restresult<R> unsupportedVersion() {
        return new Restresult<>(4041,
            "Sentinel client not supported for parameter flow control (unsupported version or dependency absent)");
    }

}
