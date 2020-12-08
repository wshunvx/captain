package com.netflix.eureka.found.resources.actuator.gateway;

import static com.netflix.eureka.command.CommandConstants.URL_MATCH_STRATEGY_EXACT;
import static com.netflix.eureka.command.CommandConstants.URL_MATCH_STRATEGY_PREFIX;
import static com.netflix.eureka.command.CommandConstants.URL_MATCH_STRATEGY_REGEX;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.netflix.eureka.dashboard.datasource.entity.gateway.ApiPredicateItemEntity;
import com.netflix.eureka.dashboard.domain.vo.gateway.api.AddApiReqVo;
import com.netflix.eureka.dashboard.domain.vo.gateway.api.ApiPredicateItemVo;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.repository.gateway.InMemApiDefinitionStore;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Produces({"application/xml", "application/json"})
public class GatewayApiResource {

    private final Logger logger = LoggerFactory.getLogger(GatewayApiResource.class);

    private InMemApiDefinitionStore repository;
    private SentinelApiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;

    GatewayApiResource(SentinelApiClient sentinelApiClient, PeerAwareInstanceRegistry instanceRegistry, InMemApiDefinitionStore repository) {
    	this.repository = repository;
    	this.sentinelApiClient = sentinelApiClient;
    	this.instanceRegistry = instanceRegistry;
    }
    
    @GET
    public Restresult<List<ApiDefinitionEntity>> queryApis(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	List<ApiDefinitionEntity> apis = null;
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
    		if(instanceInfo != null) {
    			apis = sentinelApiClient.fetchApis(app, instanceInfo.getHomePageUrl()).get();
    		}
            
        	if(apis != null) {
        		repository.saveAll(apis);
        	}
        	
            return new Restresult<>(apis);
        } catch (Throwable throwable) {
            logger.error("queryApis error:", throwable);
            return errorResponse(throwable);
        }
    }

    @POST
    public Restresult<ApiDefinitionEntity> addApi(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		MultivaluedMap<String, String> queryParams) {
    	AddApiReqVo reqVo = inEntityInternal(queryParams);
        ApiDefinitionEntity entity = new ApiDefinitionEntity();
        if (StringUtil.isBlank(reqVo.getApiName()) || StringUtil.isBlank(reqVo.getServiceId())) {
            return new Restresult<>(-1, "apiName can't be null or empty");
        }
        entity.setApiName(reqVo.getApiName());
        entity.setServiceId(reqVo.getServiceId());
        
        ApiPredicateItemVo predicateItem = reqVo.getPredicateItems();
        if (predicateItem == null) {
            return new Restresult<>(-1, "predicateItems can't empty");
        }

        ApiPredicateItemEntity predicateItemEntity = new ApiPredicateItemEntity();

        Integer matchStrategy = predicateItem.getMatchStrategy();
        if (!Arrays.asList(URL_MATCH_STRATEGY_EXACT, URL_MATCH_STRATEGY_PREFIX, URL_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
            return new Restresult<>(-1, "invalid matchStrategy: " + matchStrategy);
        }
        predicateItemEntity.setMatchStrategy(matchStrategy);

        String pattern = predicateItem.getPattern();
        if (StringUtil.isBlank(pattern)) {
            return new Restresult<>(-1, "pattern can't be null or empty");
        }
        predicateItemEntity.setPattern(pattern);
        
        entity.setPredicateItems(predicateItemEntity);

        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtCreate(new Date());
        		repository.save(entity);
        		boolean status = publishApis(app, instanceInfo.getHomePageUrl());
        		logger.warn("publish gateway apis fail after add, app={} | {}", entity.getApp(), status);
        	}
        } catch (Throwable throwable) {
            logger.error("add gateway api error:", throwable);
            return errorResponse(throwable);
        }

        return new Restresult<>(entity);
    }

    @PUT
    public Restresult<ApiDefinitionEntity> updateApi(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		MultivaluedMap<String, String> queryParams) {
    	AddApiReqVo reqVo = inEntityInternal(queryParams);
        Long id = reqVo.getId();
        if (id == null) {
            return new Restresult<>(-1, "id can't be null");
        }

        ApiDefinitionEntity entity = repository.findById(app, id);
        if (entity == null) {
            return new Restresult<>(-1, "api does not exist, id=" + id);
        }

        ApiPredicateItemVo predicateItem = reqVo.getPredicateItems();
        if (predicateItem == null) {
            return new Restresult<>(-1, "predicateItems can't empty");
        }

        ApiPredicateItemEntity predicateItemEntity = new ApiPredicateItemEntity();

        int matchStrategy = predicateItem.getMatchStrategy();
        if (!Arrays.asList(URL_MATCH_STRATEGY_EXACT, URL_MATCH_STRATEGY_PREFIX, URL_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
            return new Restresult<>(-1, "Invalid matchStrategy: " + matchStrategy);
        }
        predicateItemEntity.setMatchStrategy(matchStrategy);

        String pattern = predicateItem.getPattern();
        if (StringUtil.isBlank(pattern)) {
            return new Restresult<>(-1, "pattern can't be null or empty");
        }
        predicateItemEntity.setPattern(pattern);
        entity.setPredicateItems(predicateItemEntity);

        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtModified(new Date());
        		repository.save(entity);
        		boolean status = publishApis(app, instanceInfo.getHomePageUrl());
        		logger.warn("publish gateway apis fail after add, update={} | {}", entity.getApp(), status);
        	}
        } catch (Throwable throwable) {
        	logger.error("update gateway api error:", throwable);
            return errorResponse(throwable);
        }
        
        return new Restresult<>(entity);
    }

    @DELETE
    public Restresult<Long> deleteApi(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId, @QueryParam("id") Long id) {
        if (id == null) {
            return new Restresult<>(-1, "id can't be null");
        }

        ApiDefinitionEntity oldEntity = repository.delete(app, id);
        if (oldEntity == null) {
            return new Restresult<>();
        }

        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		if (!publishApis(app, instanceInfo.getHomePageUrl())) {
        			logger.warn("publish gateway apis fail after delete");
                }
        	}
        } catch (Throwable throwable) {
        	logger.error("update gateway api error:", throwable);
            return errorResponse(throwable);
        }

        return new Restresult<>(id);
    }
    
    private AddApiReqVo inEntityInternal(MultivaluedMap<String, String> queryParams) {
    	AddApiReqVo addApiReqVo = new AddApiReqVo();
    	String id = queryParams.getFirst("id");
    	if(StringUtil.isNotEmpty(id)) {
    		addApiReqVo.setId(Long.valueOf(id));
    	}
    	addApiReqVo.setServiceId(queryParams.getFirst("serviceId"));
    	addApiReqVo.setApiName(queryParams.getFirst("apiName"));
    	
    	ApiPredicateItemVo apiPredicateItemVo = new ApiPredicateItemVo();
    	String matchStrategy = queryParams.getFirst("matchStrategy");
    	if(StringUtil.isNotEmpty(matchStrategy)) {
    		apiPredicateItemVo.setMatchStrategy(Integer.valueOf(matchStrategy));
    	}
    	String pattern = queryParams.getFirst("pattern");
    	if(StringUtil.isNotEmpty(pattern)) {
    		apiPredicateItemVo.setPattern(pattern);
    	}
    	addApiReqVo.setPredicateItems(apiPredicateItemVo);
    	return addApiReqVo;
    }
    
    private <T> Restresult<T> errorResponse(Throwable ex) {
        return new Restresult<>(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }

    private boolean publishApis(String app, String homePage) {
        List<ApiDefinitionEntity> apis = repository.findAllByApp(app);
        return sentinelApiClient.modifyApis(app, homePage, apis);
    }
}
