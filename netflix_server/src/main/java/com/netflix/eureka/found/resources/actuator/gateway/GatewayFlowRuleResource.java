package com.netflix.eureka.found.resources.actuator.gateway;

import static com.netflix.eureka.command.CommandConstants.PARAM_MATCH_STRATEGY_CONTAINS;
import static com.netflix.eureka.command.CommandConstants.PARAM_MATCH_STRATEGY_EXACT;
import static com.netflix.eureka.command.CommandConstants.PARAM_MATCH_STRATEGY_REGEX;
import static com.netflix.eureka.command.CommandConstants.PARAM_PARSE_STRATEGY_CLIENT_IP;
import static com.netflix.eureka.command.CommandConstants.PARAM_PARSE_STRATEGY_COOKIE;
import static com.netflix.eureka.command.CommandConstants.PARAM_PARSE_STRATEGY_HEADER;
import static com.netflix.eureka.command.CommandConstants.PARAM_PARSE_STRATEGY_HOST;
import static com.netflix.eureka.command.CommandConstants.PARAM_PARSE_STRATEGY_URL_PARAM;
import static com.netflix.eureka.command.CommandConstants.RESOURCE_MODE_CUSTOM_API_NAME;
import static com.netflix.eureka.command.CommandConstants.RESOURCE_MODE_ROUTE_ID;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.CONTROL_BEHAVIOR_DEFAULT;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.FLOW_GRADE_QPS;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.FLOW_GRADE_THREAD;
import static com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity.INTERVAL_UNIT_DAY;
import static com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity.INTERVAL_UNIT_HOUR;
import static com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity.INTERVAL_UNIT_MINUTE;
import static com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity.INTERVAL_UNIT_SECOND;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayParamFlowItemEntity;
import com.netflix.eureka.dashboard.domain.vo.gateway.rule.AddFlowRuleReqVo;
import com.netflix.eureka.dashboard.domain.vo.gateway.rule.GatewayParamFlowItemVo;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.repository.gateway.InMemGatewayFlowRuleStore;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Produces({"application/xml", "application/json"})
public class GatewayFlowRuleResource {

    private final Logger logger = LoggerFactory.getLogger(GatewayFlowRuleResource.class);

    private InMemGatewayFlowRuleStore repository;
    private SentinelApiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;

    GatewayFlowRuleResource(SentinelApiClient sentinelApiClient, PeerAwareInstanceRegistry instanceRegistry, InMemGatewayFlowRuleStore repository) {
    	this.repository = repository;
    	this.sentinelApiClient = sentinelApiClient;
    	this.instanceRegistry = instanceRegistry;
    }
    
    @GET
    public Restresult<List<GatewayFlowRuleEntity>> queryFlowRules(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	List<GatewayFlowRuleEntity> rules = null;
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		rules = sentinelApiClient.fetchGatewayFlowRules(app, instanceInfo.getHomePageUrl()).get();
        	}
        	
        	if(rules != null) {
        		repository.saveAll(rules);
        	}
            
            return new Restresult<>(rules);
        } catch (Throwable throwable) {
            logger.error("query gateway flow rules error:", throwable);
            return errorResponse(throwable);
        }
    }

    @POST
    public Restresult<GatewayFlowRuleEntity> addFlowRule(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		MultivaluedMap<String, String> queryParams) {
    	AddFlowRuleReqVo reqVo = inEntityInternal(queryParams);
        GatewayFlowRuleEntity entity = new GatewayFlowRuleEntity();
        Integer resourceMode = reqVo.getResourceMode();
        if (resourceMode == null) {
            return new Restresult<>(-1, "resourceMode can't be null");
        }
        if (!Arrays.asList(RESOURCE_MODE_ROUTE_ID, RESOURCE_MODE_CUSTOM_API_NAME).contains(resourceMode)) {
            return new Restresult<>(-1, "invalid resourceMode: " + resourceMode);
        }
        entity.setResourceMode(resourceMode);

        String resource = reqVo.getResource();
        if (StringUtil.isBlank(resource)) {
            return new Restresult<>(-1, "resource can't be null or empty");
        }
        entity.setResource(resource.trim());

        GatewayParamFlowItemVo paramItem = reqVo.getParamItem();
        if (paramItem != null) {
            GatewayParamFlowItemEntity itemEntity = new GatewayParamFlowItemEntity();
            entity.setParamItem(itemEntity);

            Integer parseStrategy = paramItem.getParseStrategy();
            if (!Arrays.asList(PARAM_PARSE_STRATEGY_CLIENT_IP, PARAM_PARSE_STRATEGY_HOST, PARAM_PARSE_STRATEGY_HEADER
                    , PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                return new Restresult<>(-1, "invalid parseStrategy: " + parseStrategy);
            }
            itemEntity.setParseStrategy(paramItem.getParseStrategy());

            if (Arrays.asList(PARAM_PARSE_STRATEGY_HEADER, PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                String fieldName = paramItem.getFieldName();
                if (StringUtil.isBlank(fieldName)) {
                    return new Restresult<>(-1, "fieldName can't be null or empty");
                }
                itemEntity.setFieldName(paramItem.getFieldName());
            }

            String pattern = paramItem.getPattern();
            if (StringUtil.isNotEmpty(pattern)) {
                itemEntity.setPattern(pattern);
                Integer matchStrategy = paramItem.getMatchStrategy();
                if (!Arrays.asList(PARAM_MATCH_STRATEGY_EXACT, PARAM_MATCH_STRATEGY_CONTAINS, PARAM_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
                    return new Restresult<>(-1, "invalid matchStrategy: " + matchStrategy);
                }
                itemEntity.setMatchStrategy(matchStrategy);
            }
        }

        Integer grade = reqVo.getGrade();
        if (grade == null) {
            return new Restresult<>(-1, "grade can't be null");
        }
        if (!Arrays.asList(FLOW_GRADE_THREAD, FLOW_GRADE_QPS).contains(grade)) {
            return new Restresult<>(-1, "invalid grade: " + grade);
        }
        entity.setGrade(grade);

        Double count = reqVo.getCount();
        if (count == null) {
            return new Restresult<>(-1, "count can't be null");
        }
        if (count < 0) {
            return new Restresult<>(-1, "count should be at lease zero");
        }
        entity.setCount(count);

        Long interval = reqVo.getInterval();
        if (interval == null) {
            return new Restresult<>(-1, "interval can't be null");
        }
        if (interval <= 0) {
            return new Restresult<>(-1, "interval should be greater than zero");
        }
        entity.setInterval(interval);

        Integer intervalUnit = reqVo.getIntervalUnit();
        if (intervalUnit == null) {
            return new Restresult<>(-1, "intervalUnit can't be null");
        }
        if (!Arrays.asList(INTERVAL_UNIT_SECOND, INTERVAL_UNIT_MINUTE, INTERVAL_UNIT_HOUR, INTERVAL_UNIT_DAY).contains(intervalUnit)) {
            return new Restresult<>(-1, "Invalid intervalUnit: " + intervalUnit);
        }
        entity.setIntervalUnit(intervalUnit);

        Integer controlBehavior = reqVo.getControlBehavior();
        if (controlBehavior == null) {
            return new Restresult<>(-1, "controlBehavior can't be null");
        }
        if (!Arrays.asList(CONTROL_BEHAVIOR_DEFAULT, CONTROL_BEHAVIOR_RATE_LIMITER).contains(controlBehavior)) {
            return new Restresult<>(-1, "invalid controlBehavior: " + controlBehavior);
        }
        entity.setControlBehavior(controlBehavior);

        if (CONTROL_BEHAVIOR_DEFAULT == controlBehavior) {
            Integer burst = reqVo.getBurst();
            if (burst == null) {
                return new Restresult<>(-1, "burst can't be null");
            }
            if (burst < 0) {
                return new Restresult<>(-1, "invalid burst: " + burst);
            }
            entity.setBurst(burst);
        } else if (CONTROL_BEHAVIOR_RATE_LIMITER == controlBehavior) {
            Integer maxQueueingTimeoutMs = reqVo.getMaxQueueingTimeoutMs();
            if (maxQueueingTimeoutMs == null) {
                return new Restresult<>(-1, "maxQueueingTimeoutMs can't be null");
            }
            if (maxQueueingTimeoutMs < 0) {
                return new Restresult<>(-1, "invalid maxQueueingTimeoutMs: " + maxQueueingTimeoutMs);
            }
            entity.setMaxQueueingTimeoutMs(maxQueueingTimeoutMs);
        }
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
                entity.setInstanceId(instanceId);
                entity.setGmtModified(new Date());
                repository.save(entity);
                boolean status = publishRules(app, instanceInfo.getHomePageUrl());
                logger.warn("publish gateway apis fail after add, app={} | {}", entity.getApp(), status);
        	}
        } catch (Throwable throwable) {
            logger.error("add gateway flow rule error:", throwable);
            return errorResponse(throwable);
        }

        return new Restresult<>(entity);
    }

    @PUT
    public Restresult<GatewayFlowRuleEntity> updateFlowRule(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		MultivaluedMap<String, String> queryParams) {
    	AddFlowRuleReqVo reqVo = inEntityInternal(queryParams);
        Long id = reqVo.getId();
        if (id == null) {
            return new Restresult<>(-1, "id can't be null");
        }

        GatewayFlowRuleEntity entity = repository.findById(app, id);
        if (entity == null) {
            return new Restresult<>(-1, "gateway flow rule does not exist, id=" + id);
        }

        GatewayParamFlowItemVo paramItem = reqVo.getParamItem();
        if (paramItem != null) {
            GatewayParamFlowItemEntity itemEntity = new GatewayParamFlowItemEntity();
            entity.setParamItem(itemEntity);

            Integer parseStrategy = paramItem.getParseStrategy();
            if (!Arrays.asList(PARAM_PARSE_STRATEGY_CLIENT_IP, PARAM_PARSE_STRATEGY_HOST, PARAM_PARSE_STRATEGY_HEADER
                    , PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                return new Restresult<>(-1, "invalid parseStrategy: " + parseStrategy);
            }
            itemEntity.setParseStrategy(paramItem.getParseStrategy());

            if (Arrays.asList(PARAM_PARSE_STRATEGY_HEADER, PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                String fieldName = paramItem.getFieldName();
                if (StringUtil.isBlank(fieldName)) {
                    return new Restresult<>(-1, "fieldName can't be null or empty");
                }
                itemEntity.setFieldName(paramItem.getFieldName());
            }

            String pattern = paramItem.getPattern();
            if (StringUtil.isNotEmpty(pattern)) {
                itemEntity.setPattern(pattern);
                Integer matchStrategy = paramItem.getMatchStrategy();
                if (!Arrays.asList(PARAM_MATCH_STRATEGY_EXACT, PARAM_MATCH_STRATEGY_CONTAINS, PARAM_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
                    return new Restresult<>(-1, "invalid matchStrategy: " + matchStrategy);
                }
                itemEntity.setMatchStrategy(matchStrategy);
            }
        } else {
            entity.setParamItem(null);
        }

        Integer grade = reqVo.getGrade();
        if (grade == null) {
            return new Restresult<>(-1, "grade can't be null");
        }
        if (!Arrays.asList(FLOW_GRADE_THREAD, FLOW_GRADE_QPS).contains(grade)) {
            return new Restresult<>(-1, "invalid grade: " + grade);
        }
        entity.setGrade(grade);

        Double count = reqVo.getCount();
        if (count == null) {
            return new Restresult<>(-1, "count can't be null");
        }
        if (count < 0) {
            return new Restresult<>(-1, "count should be at lease zero");
        }
        entity.setCount(count);

        Long interval = reqVo.getInterval();
        if (interval == null) {
            return new Restresult<>(-1, "interval can't be null");
        }
        if (interval <= 0) {
            return new Restresult<>(-1, "interval should be greater than zero");
        }
        entity.setInterval(interval);

        Integer intervalUnit = reqVo.getIntervalUnit();
        if (intervalUnit == null) {
            return new Restresult<>(-1, "intervalUnit can't be null");
        }
        if (!Arrays.asList(INTERVAL_UNIT_SECOND, INTERVAL_UNIT_MINUTE, INTERVAL_UNIT_HOUR, INTERVAL_UNIT_DAY).contains(intervalUnit)) {
            return new Restresult<>(-1, "Invalid intervalUnit: " + intervalUnit);
        }
        entity.setIntervalUnit(intervalUnit);

        Integer controlBehavior = reqVo.getControlBehavior();
        if (controlBehavior == null) {
            return new Restresult<>(-1, "controlBehavior can't be null");
        }
        if (!Arrays.asList(CONTROL_BEHAVIOR_DEFAULT, CONTROL_BEHAVIOR_RATE_LIMITER).contains(controlBehavior)) {
            return new Restresult<>(-1, "invalid controlBehavior: " + controlBehavior);
        }
        entity.setControlBehavior(controlBehavior);

        if (CONTROL_BEHAVIOR_DEFAULT == controlBehavior) {
            Integer burst = reqVo.getBurst();
            if (burst == null) {
                return new Restresult<>(-1, "burst can't be null");
            }
            if (burst < 0) {
                return new Restresult<>(-1, "invalid burst: " + burst);
            }
            entity.setBurst(burst);
        } else if (CONTROL_BEHAVIOR_RATE_LIMITER == controlBehavior) {
            Integer maxQueueingTimeoutMs = reqVo.getMaxQueueingTimeoutMs();
            if (maxQueueingTimeoutMs == null) {
                return new Restresult<>(-1, "maxQueueingTimeoutMs can't be null");
            }
            if (maxQueueingTimeoutMs < 0) {
                return new Restresult<>(-1, "invalid maxQueueingTimeoutMs: " + maxQueueingTimeoutMs);
            }
            entity.setMaxQueueingTimeoutMs(maxQueueingTimeoutMs);
        }

        Date date = new Date();
        entity.setGmtModified(date);

        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		repository.save(entity);
        		boolean status = publishRules(app, instanceInfo.getHomePageUrl());
                logger.warn("publish gateway apis fail after update, app={} | {}", entity.getApp(), status);
        	}
        } catch (Throwable throwable) {
            logger.error("update gateway flow rule error:", throwable);
            return errorResponse(throwable);
        }

        return new Restresult<>(entity);
    }


    @DELETE
    @Path("{id}")
    public Restresult<Long> deleteFlowRule(@PathParam("id") Long id, @QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        GatewayFlowRuleEntity oldEntity = repository.delete(app, id);
        if (oldEntity == null) {
            return new Restresult<>();
        }
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		if (!publishRules(app, instanceInfo.getHomePageUrl())) {
        			logger.warn("publish gateway flow rules fail after delete");
                }
        	}
        } catch (Throwable throwable) {
            logger.error("delete gateway flow rule error:", throwable);
            return errorResponse(throwable);
        }

        return new Restresult<>(id);
    }
    
    private AddFlowRuleReqVo inEntityInternal(MultivaluedMap<String, String> queryParams) {
    	AddFlowRuleReqVo addFlowRuleReqVo = new AddFlowRuleReqVo();
    	String id = queryParams.getFirst("id");
    	if(StringUtil.isNotEmpty(id)) {
    		addFlowRuleReqVo.setId(Long.valueOf(id));
    	}
    	addFlowRuleReqVo.setResource(queryParams.getFirst("resource"));
    	String resourceMode = queryParams.getFirst("resourceMode");
    	if(StringUtil.isNotEmpty(resourceMode)) {
    		addFlowRuleReqVo.setResourceMode(Integer.valueOf(resourceMode));
    	}
    	String grade = queryParams.getFirst("grade");
    	if(StringUtil.isNotEmpty(grade)) {
    		addFlowRuleReqVo.setGrade(Integer.valueOf(grade));
    	}
    	String count = queryParams.getFirst("count");
    	if(StringUtil.isNotEmpty(count)) {
    		addFlowRuleReqVo.setCount(Double.valueOf(count));
    	}
    	String interval = queryParams.getFirst("interval");
    	if(StringUtil.isNotEmpty(interval)) {
    		addFlowRuleReqVo.setInterval(Long.valueOf(interval));
    	}
    	String intervalUnit = queryParams.getFirst("intervalUnit");
    	if(StringUtil.isNotEmpty(intervalUnit)) {
    		addFlowRuleReqVo.setIntervalUnit(Integer.valueOf(intervalUnit));
    	}
    	String controlBehavior = queryParams.getFirst("controlBehavior");
    	if(StringUtil.isNotEmpty(controlBehavior)) {
    		addFlowRuleReqVo.setControlBehavior(Integer.valueOf(controlBehavior));
    	}
    	String burst = queryParams.getFirst("burst");
    	if(StringUtil.isNotEmpty(burst)) {
    		addFlowRuleReqVo.setBurst(Integer.valueOf(burst));
    	}
    	String maxQueueingTimeoutMs = queryParams.getFirst("maxQueueingTimeoutMs");
    	if(StringUtil.isNotEmpty(maxQueueingTimeoutMs)) {
    		addFlowRuleReqVo.setMaxQueueingTimeoutMs(Integer.valueOf(maxQueueingTimeoutMs));
    	}
    	String paramItem = queryParams.getFirst("paramItem");
    	if(StringUtil.isNotEmpty(paramItem)) {
    		addFlowRuleReqVo.setParamItem(JSONFormatter.fromJSON(paramItem, GatewayParamFlowItemVo.class));
    	}
    	return addFlowRuleReqVo;
    }
    
    private <T> Restresult<T> errorResponse(Throwable ex) {
        return new Restresult<>(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }

    private boolean publishRules(String app, String homePage) {
        List<GatewayFlowRuleEntity> rules = repository.findAllByApp(app);
        return sentinelApiClient.modifyGatewayFlowRules(app, homePage, rules);
    }
}
