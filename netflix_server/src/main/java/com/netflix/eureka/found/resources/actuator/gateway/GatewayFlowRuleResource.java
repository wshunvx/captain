package com.netflix.eureka.found.resources.actuator.gateway;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.CONTROL_BEHAVIOR_DEFAULT;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.FLOW_GRADE_QPS;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.FLOW_GRADE_THREAD;
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
import static com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity.INTERVAL_UNIT_DAY;
import static com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity.INTERVAL_UNIT_HOUR;
import static com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity.INTERVAL_UNIT_MINUTE;
import static com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity.INTERVAL_UNIT_SECOND;

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
import com.netflix.eureka.command.Resource;
import com.netflix.eureka.command.Resource.RuleType;
import com.netflix.eureka.dashboard.client.HttpapiClient;
import com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayParamFlowItemEntity;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.repository.RuleRepositoryAdapter;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Produces({"application/xml", "application/json"})
public class GatewayFlowRuleResource {

    private final Logger logger = LoggerFactory.getLogger(GatewayFlowRuleResource.class);

    private HttpapiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    private RuleRepositoryAdapter<GatewayFlowRuleEntity> repository;

    GatewayFlowRuleResource(HttpapiClient sentinelApiClient, PeerAwareInstanceRegistry instanceRegistry, RuleRepositoryAdapter<GatewayFlowRuleEntity> repository) {
    	this.sentinelApiClient = sentinelApiClient;
    	this.instanceRegistry = instanceRegistry;
    	this.repository = repository;
    }
    
    @GET
    public Restresult<List<GatewayFlowRuleEntity>> queryFlowRules(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
        try {
        	List<GatewayFlowRuleEntity> rules = new ArrayList<GatewayFlowRuleEntity>();
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		Collection<GatewayFlowRuleEntity> list = repository.getRule(new Resource(RuleType.GATEWAY_RULE_TYPE, instanceId));
        		if(list == null || list.isEmpty()) {
        			list = sentinelApiClient.fetchGatewayFlowRules(app, instanceInfo.getHomePageUrl()).get();
        			if(list != null) {
                    	rules.addAll(list);
                    }
                    repository.setRule(new Resource(RuleType.GATEWAY_RULE_TYPE, instanceId), rules);
        		} else {
        			rules.addAll(list);
        		}
        	}
            return Restresult.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("query gateway flow rules error:", throwable);
            return errorResponse(throwable);
        }
    }

    @POST
    public Restresult<GatewayFlowRuleEntity> addFlowRule(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		MultivaluedMap<String, String> queryParams) {
    	GatewayFlowRuleEntity entity = inEntityInternal(queryParams);
        Integer resourceMode = entity.getResourceMode();
        if (resourceMode == null) {
            return Restresult.ofFailure(-1, "resourceMode can't be null");
        }
        if (!Arrays.asList(RESOURCE_MODE_ROUTE_ID, RESOURCE_MODE_CUSTOM_API_NAME).contains(resourceMode)) {
            return Restresult.ofFailure(-1, "invalid resourceMode: " + resourceMode);
        }

        String resource = entity.getResource();
        if (StringUtil.isBlank(resource)) {
            return Restresult.ofFailure(-1, "resource can't be null or empty");
        }

        GatewayParamFlowItemEntity paramItem = entity.getParamItem();
        if (paramItem != null) {
            Integer parseStrategy = paramItem.getParseStrategy();
            if (!Arrays.asList(PARAM_PARSE_STRATEGY_CLIENT_IP, PARAM_PARSE_STRATEGY_HOST, PARAM_PARSE_STRATEGY_HEADER
                    , PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                return Restresult.ofFailure(-1, "invalid parseStrategy: " + parseStrategy);
            }

            if (Arrays.asList(PARAM_PARSE_STRATEGY_HEADER, PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                String fieldName = paramItem.getFieldName();
                if (StringUtil.isBlank(fieldName)) {
                    return Restresult.ofFailure(-1, "fieldName can't be null or empty");
                }
            }

            String pattern = paramItem.getPattern();
            if (StringUtil.isNotEmpty(pattern)) {
                Integer matchStrategy = paramItem.getMatchStrategy();
                if (!Arrays.asList(PARAM_MATCH_STRATEGY_EXACT, PARAM_MATCH_STRATEGY_CONTAINS, PARAM_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
                    return Restresult.ofFailure(-1, "invalid matchStrategy: " + matchStrategy);
                }
            }
        }

        Integer grade = entity.getGrade();
        if (grade == null) {
            return Restresult.ofFailure(-1, "grade can't be null");
        }
        if (!Arrays.asList(FLOW_GRADE_THREAD, FLOW_GRADE_QPS).contains(grade)) {
            return Restresult.ofFailure(-1, "invalid grade: " + grade);
        }

        Double count = entity.getCount();
        if (count == null) {
            return Restresult.ofFailure(-1, "count can't be null");
        }
        if (count < 0) {
            return Restresult.ofFailure(-1, "count should be at lease zero");
        }

        Long interval = entity.getInterval();
        if (interval == null) {
            return Restresult.ofFailure(-1, "interval can't be null");
        }
        if (interval <= 0) {
            return Restresult.ofFailure(-1, "interval should be greater than zero");
        }

        Integer intervalUnit = entity.getIntervalUnit();
        if (intervalUnit == null) {
            return Restresult.ofFailure(-1, "intervalUnit can't be null");
        }
        if (!Arrays.asList(INTERVAL_UNIT_SECOND, INTERVAL_UNIT_MINUTE, INTERVAL_UNIT_HOUR, INTERVAL_UNIT_DAY).contains(intervalUnit)) {
            return Restresult.ofFailure(-1, "Invalid intervalUnit: " + intervalUnit);
        }

        Integer controlBehavior = entity.getControlBehavior();
        if (controlBehavior == null) {
            return Restresult.ofFailure(-1, "controlBehavior can't be null");
        }
        if (!Arrays.asList(CONTROL_BEHAVIOR_DEFAULT, CONTROL_BEHAVIOR_RATE_LIMITER).contains(controlBehavior)) {
            return Restresult.ofFailure(-1, "invalid controlBehavior: " + controlBehavior);
        }

        if (CONTROL_BEHAVIOR_DEFAULT == controlBehavior) {
            Integer burst = entity.getBurst();
            if (burst == null) {
                return Restresult.ofFailure(-1, "burst can't be null");
            }
            if (burst < 0) {
                return Restresult.ofFailure(-1, "invalid burst: " + burst);
            }
        } else if (CONTROL_BEHAVIOR_RATE_LIMITER == controlBehavior) {
            Integer maxQueueingTimeoutMs = entity.getMaxQueueingTimeoutMs();
            if (maxQueueingTimeoutMs == null) {
                return Restresult.ofFailure(-1, "maxQueueingTimeoutMs can't be null");
            }
            if (maxQueueingTimeoutMs < 0) {
                return Restresult.ofFailure(-1, "invalid maxQueueingTimeoutMs: " + maxQueueingTimeoutMs);
            }
        }
        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		entity.setApp(app);
                entity.setInstanceId(instanceId);
                entity.setGmtModified(new Date());
                if(repository.setRule(new Resource(RuleType.GATEWAY_RULE_TYPE, instanceId), entity)) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
            		if(!status) {
            			logger.warn("Publish degrade rules failed, app={} | {}", app, status);
            		}
        		}
        	}
        } catch (Throwable throwable) {
            logger.error("add gateway flow rule error:", throwable);
            return errorResponse(throwable);
        }

        return Restresult.ofSuccess(entity);
    }

    @PUT
    public Restresult<GatewayFlowRuleEntity> updateFlowRule(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		MultivaluedMap<String, String> queryParams) {
    	GatewayFlowRuleEntity entity = inEntityInternal(queryParams);
    	if(StringUtils.isEmpty(entity.getId())) {
    		return Restresult.ofFailure(-1, "Unable to get the value of id.");
    	}
        GatewayParamFlowItemEntity paramItem = entity.getParamItem();
        if (paramItem != null) {
            Integer parseStrategy = paramItem.getParseStrategy();
            if (!Arrays.asList(PARAM_PARSE_STRATEGY_CLIENT_IP, PARAM_PARSE_STRATEGY_HOST, PARAM_PARSE_STRATEGY_HEADER
                    , PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                return Restresult.ofFailure(-1, "invalid parseStrategy: " + parseStrategy);
            }

            if (Arrays.asList(PARAM_PARSE_STRATEGY_HEADER, PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                String fieldName = paramItem.getFieldName();
                if (StringUtil.isBlank(fieldName)) {
                    return Restresult.ofFailure(-1, "fieldName can't be null or empty");
                }
            }

            String pattern = paramItem.getPattern();
            if (StringUtil.isNotEmpty(pattern)) {
                Integer matchStrategy = paramItem.getMatchStrategy();
                if (!Arrays.asList(PARAM_MATCH_STRATEGY_EXACT, PARAM_MATCH_STRATEGY_CONTAINS, PARAM_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
                    return Restresult.ofFailure(-1, "invalid matchStrategy: " + matchStrategy);
                }
            }
        }

        Integer grade = entity.getGrade();
        if (grade == null) {
            return Restresult.ofFailure(-1, "grade can't be null");
        }
        if (!Arrays.asList(FLOW_GRADE_THREAD, FLOW_GRADE_QPS).contains(grade)) {
            return Restresult.ofFailure(-1, "invalid grade: " + grade);
        }

        Double count = entity.getCount();
        if (count == null) {
            return Restresult.ofFailure(-1, "count can't be null");
        }
        if (count < 0) {
            return Restresult.ofFailure(-1, "count should be at lease zero");
        }

        Long interval = entity.getInterval();
        if (interval == null) {
            return Restresult.ofFailure(-1, "interval can't be null");
        }
        if (interval <= 0) {
            return Restresult.ofFailure(-1, "interval should be greater than zero");
        }

        Integer intervalUnit = entity.getIntervalUnit();
        if (intervalUnit == null) {
            return Restresult.ofFailure(-1, "intervalUnit can't be null");
        }
        if (!Arrays.asList(INTERVAL_UNIT_SECOND, INTERVAL_UNIT_MINUTE, INTERVAL_UNIT_HOUR, INTERVAL_UNIT_DAY).contains(intervalUnit)) {
            return Restresult.ofFailure(-1, "Invalid intervalUnit: " + intervalUnit);
        }

        Integer controlBehavior = entity.getControlBehavior();
        if (controlBehavior == null) {
            return Restresult.ofFailure(-1, "controlBehavior can't be null");
        }
        if (!Arrays.asList(CONTROL_BEHAVIOR_DEFAULT, CONTROL_BEHAVIOR_RATE_LIMITER).contains(controlBehavior)) {
            return Restresult.ofFailure(-1, "invalid controlBehavior: " + controlBehavior);
        }

        if (CONTROL_BEHAVIOR_DEFAULT == controlBehavior) {
            Integer burst = entity.getBurst();
            if (burst == null) {
                return Restresult.ofFailure(-1, "burst can't be null");
            }
            if (burst < 0) {
                return Restresult.ofFailure(-1, "invalid burst: " + burst);
            }
        } else if (CONTROL_BEHAVIOR_RATE_LIMITER == controlBehavior) {
            Integer maxQueueingTimeoutMs = entity.getMaxQueueingTimeoutMs();
            if (maxQueueingTimeoutMs == null) {
                return Restresult.ofFailure(-1, "maxQueueingTimeoutMs can't be null");
            }
            if (maxQueueingTimeoutMs < 0) {
                return Restresult.ofFailure(-1, "invalid maxQueueingTimeoutMs: " + maxQueueingTimeoutMs);
            }
        }

        Date date = new Date();
        entity.setGmtModified(date);

        try {
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		if(repository.setRule(new Resource(RuleType.GATEWAY_RULE_TYPE, instanceId), entity)) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
        			if(!status) {
        				logger.warn("Publish degrade rules failed, app={} | {}", app, status);
        			}
        		}
        	}
        } catch (Throwable throwable) {
            logger.error("update gateway flow rule error:", throwable);
            return errorResponse(throwable);
        }

        return Restresult.ofSuccess(entity);
    }


    @DELETE
    public Restresult<Long> deleteFlowRule(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId,
    		@QueryParam("id") Long id) {
        try {
        	GatewayFlowRuleEntity entity = repository.getRule(new Resource(RuleType.GATEWAY_RULE_TYPE, instanceId), id);
        	if(entity == null) {
        		return Restresult.ofFailure(-1, "Unable to get the value of id.");
        	}
        	InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		boolean remove = repository.removeRule(new Resource(RuleType.GATEWAY_RULE_TYPE, instanceId), entity);
        		if(remove) {
        			boolean status = publishRules(instanceId, instanceInfo.getHomePageUrl());
            		if(!status) {
            			repository.setRule(new Resource(RuleType.GATEWAY_RULE_TYPE, instanceId), entity);
            			logger.warn("Publish degrade rules failed, app={} | {}", app, status);
            		}
        		}
        	}
        } catch (Throwable throwable) {
            logger.error("delete gateway flow rule error:", throwable);
            return errorResponse(throwable);
        }

        return Restresult.ofSuccess(id);
    }
    
    private GatewayFlowRuleEntity inEntityInternal(MultivaluedMap<String, String> queryParams) {
    	GatewayFlowRuleEntity addFlowRuleReqVo = new GatewayFlowRuleEntity();
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
    		addFlowRuleReqVo.setParamItem(JSONFormatter.fromJSON(paramItem, GatewayParamFlowItemEntity.class));
    	}
    	return addFlowRuleReqVo;
    }
    
    private <T> Restresult<T> errorResponse(Throwable ex) {
        return Restresult.ofFailure(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }

    private boolean publishRules(String instanceId, String homePage) {
        Collection<GatewayFlowRuleEntity> rules = repository.getRule(new Resource(RuleType.GATEWAY_RULE_TYPE, instanceId));
        return sentinelApiClient.modifyGatewayFlowRules(homePage, rules);
    }
}
