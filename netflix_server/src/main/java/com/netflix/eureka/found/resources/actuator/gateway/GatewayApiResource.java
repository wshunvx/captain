package com.netflix.eureka.found.resources.actuator.gateway;

import static com.netflix.eureka.command.CommandConstants.URL_MATCH_STRATEGY_EXACT;
import static com.netflix.eureka.command.CommandConstants.URL_MATCH_STRATEGY_PREFIX;
import static com.netflix.eureka.command.CommandConstants.URL_MATCH_STRATEGY_REGEX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.command.CommandConstants;
import com.netflix.eureka.command.Resource;
import com.netflix.eureka.command.Resource.RuleType;
import com.netflix.eureka.dashboard.client.HttpapiClient;
import com.netflix.eureka.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.repository.RuleRepositoryAdapter;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Produces({"application/xml", "application/json"})
public class GatewayApiResource {

    private final Logger logger = LoggerFactory.getLogger(GatewayApiResource.class);

    private HttpapiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    private RuleRepositoryAdapter<ApiDefinitionEntity> repository;

    GatewayApiResource(HttpapiClient sentinelApiClient, PeerAwareInstanceRegistry instanceRegistry, RuleRepositoryAdapter<ApiDefinitionEntity> repository) {
    	this.sentinelApiClient = sentinelApiClient;
    	this.instanceRegistry = instanceRegistry;
    	this.repository = repository;
    }
    
    @GET
    public Restresult<List<ApiDefinitionEntity>> queryApis(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	List<ApiDefinitionEntity> apis = new ArrayList<ApiDefinitionEntity>();
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
    		if(instanceInfo != null) {
    			Collection<ApiDefinitionEntity> list = repository.getRule(new Resource(RuleType.GATEWAY_API_TYPE, instanceId));
    			if(list == null || list.isEmpty()) {
    				list = sentinelApiClient.fetchApis(app, instanceInfo.getHomePageUrl()).get();
    				if(list != null) {
    					apis.addAll(list);
                    }
                    repository.setRule(new Resource(RuleType.GATEWAY_API_TYPE, instanceId), apis);
    			} else {
    				apis.addAll(list);
        		}
    		}
            return Restresult.ofSuccess(apis);
        } catch (Throwable throwable) {
            logger.error("queryApis error:", throwable);
            return errorResponse(throwable);
        }
    }

    @POST
    public Restresult<ApiDefinitionEntity> addApi(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		MultivaluedMap<String, String> queryParams) {
    	ApiDefinitionEntity entity = inEntityInternal(queryParams);
        if (StringUtil.isBlank(entity.getApiName()) || StringUtil.isBlank(entity.getPattern())) {
            return Restresult.ofFailure(-1, "apiName can't be null or empty");
        }
        
        Integer matchStrategy = entity.getMatchStrategy();
        if (!Arrays.asList(URL_MATCH_STRATEGY_EXACT, URL_MATCH_STRATEGY_PREFIX, URL_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
            return Restresult.ofFailure(-1, "invalid matchStrategy: " + matchStrategy);
        }

        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtCreate(new Date());
        		if(repository.setRule(new Resource(RuleType.GATEWAY_API_TYPE, instanceId), entity)) {
        			boolean status = publishApis(instanceId, instanceInfo.getHomePageUrl());
            		if(!status) {
            			logger.warn("Publish gateway rules failed, app={} | {}", app, status);
            		}
        		}
        	}
        } catch (Throwable throwable) {
            logger.error("add gateway api error:", throwable);
            return errorResponse(throwable);
        }

        return Restresult.ofSuccess(entity);
    }

    @PUT
    public Restresult<ApiDefinitionEntity> updateApi(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		MultivaluedMap<String, String> queryParams) {
    	ApiDefinitionEntity entity = inEntityInternal(queryParams);
    	if(StringUtils.isEmpty(entity.getId())) {
    		return Restresult.ofFailure(-1, "Unable to get the value of id.");
    	}
        int matchStrategy = entity.getMatchStrategy();
        if (!Arrays.asList(URL_MATCH_STRATEGY_EXACT, URL_MATCH_STRATEGY_PREFIX, URL_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
            return Restresult.ofFailure(-1, "Invalid matchStrategy: " + matchStrategy);
        }

        String pattern = entity.getPattern();
        if (StringUtil.isBlank(pattern)) {
            return Restresult.ofFailure(-1, "pattern can't be null or empty");
        }

        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
        		entity.setInstanceId(instanceId);
        		entity.setGmtModified(new Date());
        		if(repository.setRule(new Resource(RuleType.GATEWAY_API_TYPE, instanceId), entity)) {
        			boolean status = publishApis(instanceId, instanceInfo.getHomePageUrl());
        			if(!status) {
        				logger.warn("Publish gateway rules failed, app={} | {}", app, status);
        			}
        		}
        	}
        } catch (Throwable throwable) {
        	logger.error("update gateway api error:", throwable);
            return errorResponse(throwable);
        }
        
        return Restresult.ofSuccess(entity);
    }

    @DELETE
    public Restresult<Long> deleteApi(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId, @QueryParam("id") Long id) {
        try {
        	ApiDefinitionEntity entity = repository.getRule(new Resource(RuleType.GATEWAY_API_TYPE, instanceId), id);
        	if(entity == null) {
        		return Restresult.ofFailure(-1, "Unable to get the value of id.");
        	}
        	
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		boolean remove = repository.removeRule(new Resource(RuleType.GATEWAY_API_TYPE, instanceId), entity);
        		if(remove) {
        			boolean status = publishApis(instanceId, instanceInfo.getHomePageUrl());
            		if(!status) {
            			repository.setRule(new Resource(RuleType.GATEWAY_API_TYPE, instanceId), entity);
            			logger.warn("Publish degrade rules failed, app={} | {}", app, status);
            		}
        		}
        	}
        } catch (Throwable throwable) {
        	logger.error("update gateway api error:", throwable);
            return errorResponse(throwable);
        }

        return Restresult.ofSuccess(id);
    }
    
    private ApiDefinitionEntity inEntityInternal(MultivaluedMap<String, String> queryParams) {
    	ApiDefinitionEntity addApiReqVo = new ApiDefinitionEntity();
    	String id = queryParams.getFirst("id");
    	if(StringUtil.isNotEmpty(id)) {
    		addApiReqVo.setId(Long.valueOf(id));
    	}
    	addApiReqVo.setServiceId(queryParams.getFirst("serviceId"));
    	addApiReqVo.setApiName(queryParams.getFirst("apiName"));
    	
    	String matchStrategy = queryParams.getFirst("matchStrategy");
    	if(StringUtil.isNotEmpty(matchStrategy)) {
    		addApiReqVo.setMatchStrategy(Integer.valueOf(matchStrategy));
    	}
    	String pattern = queryParams.getFirst("pattern");
    	if(StringUtil.isNotEmpty(pattern)) {
    		addApiReqVo.setPattern(pattern);
    	}
    	String url = queryParams.getFirst("url");
    	if(StringUtil.isNotEmpty(url)) {
    		addApiReqVo.setUrl(url);
    	}
    	String stripPrefix = queryParams.getFirst("stripPrefix");
    	if(StringUtil.isNotEmpty(stripPrefix)) {
    		addApiReqVo.setStripPrefix(Boolean.valueOf(stripPrefix) ? CommandConstants.STRIP_PREFIX_ROUTE_TRUE : CommandConstants.STRIP_PREFIX_ROUTE_FALSE);
    	}
    	return addApiReqVo;
    }
    
    private <T> Restresult<T> errorResponse(Throwable ex) {
        return Restresult.ofFailure(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }

    private boolean publishApis(String instanceId, String homePage) {
    	Collection<ApiDefinitionEntity> apis = repository.getRule(new Resource(RuleType.GATEWAY_API_TYPE, instanceId));
        return sentinelApiClient.modifyApis(homePage, apis);
    }
}
