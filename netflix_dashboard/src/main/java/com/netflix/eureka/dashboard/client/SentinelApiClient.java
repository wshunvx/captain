package com.netflix.eureka.dashboard.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.netflix.eureka.command.CommandConstants;
import com.netflix.eureka.command.vo.NodeVo;
import com.netflix.eureka.common.GatewayFlowRule;
import com.netflix.eureka.common.ParamFlowRule;
import com.netflix.eureka.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.RuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.netflix.eureka.dashboard.domain.cluster.ClusterClientInfoVO;
import com.netflix.eureka.dashboard.domain.cluster.config.ClusterClientConfig;
import com.netflix.eureka.dashboard.domain.cluster.config.ServerFlowConfig;
import com.netflix.eureka.dashboard.domain.cluster.config.ServerTransportConfig;
import com.netflix.eureka.dashboard.domain.cluster.state.ClusterServerStateVO;
import com.netflix.eureka.dashboard.domain.cluster.state.ClusterStateSimpleEntity;
import com.netflix.eureka.dashboard.util.AsyncUtils;
import com.netflix.eureka.gson.JSONFormatter;

public class SentinelApiClient {
    private static Logger logger = LoggerFactory.getLogger(SentinelApiClient.class);

    private static final Charset DEFAULT_CHARSET = Charset.forName(SentinelConfig.charset());
    private static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_HEADER_CONTENT_TYPE_URLENCODED = "application/json";

    /**
     * 指标信息
     */
    private static final String RESOURCE_URL_PATH = "actuator/jsonTree";
    private static final String CLUSTER_NODE_PATH = "actuator/clusterNode";
    private static final String GET_RULES_PATH = "actuator/getRules";
    private static final String SET_RULES_PATH = "actuator/setRules";
    private static final String GET_PARAM_RULE_PATH = "actuator/getParamFlowRules";
    private static final String SET_PARAM_RULE_PATH = "actuator/setParamFlowRules";

    private static final String FETCH_CLUSTER_MODE_PATH = "getClusterMode";
    private static final String MODIFY_CLUSTER_MODE_PATH = "setClusterMode";
    
    /**
     * 集群信息
     */
    private static final String FETCH_CLUSTER_CLIENT_CONFIG_PATH = "cluster/client/fetchConfig";
    private static final String MODIFY_CLUSTER_CLIENT_CONFIG_PATH = "cluster/client/modifyConfig";
    private static final String FETCH_CLUSTER_SERVER_BASIC_INFO_PATH = "cluster/server/info";
    private static final String MODIFY_CLUSTER_SERVER_TRANSPORT_CONFIG_PATH = "cluster/server/modifyTransportConfig";
    private static final String MODIFY_CLUSTER_SERVER_FLOW_CONFIG_PATH = "cluster/server/modifyFlowConfig";
    private static final String MODIFY_CLUSTER_SERVER_NAMESPACE_SET_PATH = "cluster/server/modifyNamespaceSet";

    /**
     * 网关信息
     */
    private static final String FETCH_GATEWAY_API_PATH = "actuator/getApiDefinitions";
    private static final String MODIFY_GATEWAY_API_PATH = "actuator/setApiDefinitions";

    private static final String FETCH_GATEWAY_FLOW_RULE_PATH = "actuator/getApiRules";
    private static final String MODIFY_GATEWAY_FLOW_RULE_PATH = "actuator/setApiRules";

    private static final String FLOW_RULE_TYPE = "flow";
    private static final String DEGRADE_RULE_TYPE = "degrade";
    private static final String SYSTEM_RULE_TYPE = "system";
    private static final String AUTHORITY_TYPE = "authority";

    private CloseableHttpAsyncClient httpClient;

    public SentinelApiClient() {
        IOReactorConfig ioConfig = IOReactorConfig.custom().setConnectTimeout(3000).setSoTimeout(10000)
            .setIoThreadCount(Runtime.getRuntime().availableProcessors() * 2).build();
        httpClient = HttpAsyncClients.custom().setRedirectStrategy(new DefaultRedirectStrategy() {
            @Override
            protected boolean isRedirectable(final String method) {
                return false;
            }
        }).setMaxConnTotal(4000).setMaxConnPerRoute(1000).setDefaultIOReactorConfig(ioConfig).build();
        httpClient.start();
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
    
    private boolean isCommandNotFound(int statusCode, String body) {
        return statusCode == 400 && StringUtil.isNotEmpty(body) && body.contains(CommandConstants.MSG_UNKNOWN_COMMAND_PREFIX);
    }
    
    private StringBuilder queryString(Map<String, String> params) {
        StringBuilder queryStringBuilder = new StringBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            if (StringUtil.isEmpty(entry.getValue())) {
                continue;
            }
            String name = urlEncode(entry.getKey());
            String value = urlEncode(entry.getValue());
            if (name != null && value != null) {
                if (queryStringBuilder.length() > 0) {
                    queryStringBuilder.append('&');
                }
                queryStringBuilder.append(name).append('=').append(value);
            }
        }
        return queryStringBuilder;
    }
    
    /**
     * Build an `HttpUriRequest` in POST way.
     * 
     * @param url
     * @param params
     * @param supportEnhancedContentType see {@link #isSupportEnhancedContentType(String, String, int)}
     * @return
     */
    protected HttpUriRequest postRequest(String url, Map<String, String> params, String contentType) {
        HttpPost httpPost = new HttpPost(url);
        if (params != null && params.size() > 0) {
//            List<NameValuePair> list = new ArrayList<>(params.size());
//            for (Entry<String, String> entry : params.entrySet()) {
//                list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
//            }
            httpPost.setEntity(new EncodedJsonEntity(params, Consts.UTF_8));
//            httpPost.setEntity(new UrlEncodedFormEntity(list, Consts.UTF_8));
            httpPost.setHeader(HTTP_HEADER_CONTENT_TYPE, contentType);
        }
        return httpPost;
    }
    
    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, DEFAULT_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            logger.info("encode string error: {}", str, e);
            return null;
        }
    }
    
    private String getBody(HttpResponse response) throws Exception {
        Charset charset = null;
        try {
            String contentTypeStr = response.getFirstHeader(HTTP_HEADER_CONTENT_TYPE).getValue();
            if (StringUtil.isNotEmpty(contentTypeStr)) {
                ContentType contentType = ContentType.parse(contentTypeStr);
                charset = contentType.getCharset();
            }
        } catch (Exception ignore) {
        }
        return EntityUtils.toString(response.getEntity(), charset != null ? charset : DEFAULT_CHARSET);
    }
    
    /**
     * With no param
     * 
     * @param ip
     * @param port
     * @param api
     * @return
     */
    private CompletableFuture<String> executeCommand(String homePage, String api, boolean useHttpPost) {
        return executeCommand(homePage, api, null, useHttpPost);
    }
    
    /**
     * No app specified, force to GET
     * 
     * @param ip
     * @param port
     * @param api
     * @param params
     * @return
     */
    private CompletableFuture<String> executeCommand(String homePage, String api, Map<String, String> params, boolean useHttpPost) {
        return executeCommand(null, homePage, api, params, useHttpPost);
    }

    /**
     * Prefer to execute request using POST
     * 
     * @param app
     * @param ip
     * @param port
     * @param api
     * @param params
     * @return
     */
    private CompletableFuture<String> executeCommand(String app, String homePage, String api, Map<String, String> params, boolean useHttpPost) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (StringUtil.isBlank(homePage) || StringUtil.isBlank(api)) {
            future.completeExceptionally(new IllegalArgumentException("Bad URL or command name"));
            return future;
        }
        StringBuilder urlBuilder = new StringBuilder(homePage);
        urlBuilder.append(api);
        if (params == null) {
            params = Collections.emptyMap();
        }
        if (useHttpPost) {
        	// Using POST
            return executeCommand(
                    postRequest(urlBuilder.toString(), params, HTTP_HEADER_CONTENT_TYPE_URLENCODED));
        }
        // Using GET in older versions, append parameters after url
        if (!params.isEmpty()) {
            if (urlBuilder.indexOf("?") == -1) {
                urlBuilder.append('?');
            } else {
                urlBuilder.append('&');
            }
            urlBuilder.append(queryString(params));
        }
        return executeCommand(new HttpGet(urlBuilder.toString()));
    }
    
    private CompletableFuture<String> executeCommand(HttpUriRequest request) {
        CompletableFuture<String> future = new CompletableFuture<>();
        httpClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse response) {
                int statusCode = response.getStatusLine().getStatusCode();
                try {
                    String value = getBody(response);
                    if (isSuccess(statusCode)) {
                        future.complete(value);
                    } else {
                        if (isCommandNotFound(statusCode, value)) {
                            future.completeExceptionally(new CommandNotFoundException(request.getURI().getPath()));
                        } else {
                            future.completeExceptionally(new CommandFailedException(value));
                        }
                    }

                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                    logger.error("HTTP request failed: {}", request.getURI().toString(), ex);
                }
            }

            @Override
            public void failed(final Exception ex) {
                future.completeExceptionally(ex);
                logger.error("HTTP request failed: {}", request.getURI().toString(), ex);
            }

            @Override
            public void cancelled() {
                future.complete(null);
            }
        });
        return future;
    }
    
    public void close() throws Exception {
        httpClient.close();
    }
    
    @Nullable
    private <T> CompletableFuture<List<T>> fetchItemsAsync(String homePage, String api, String type, Class<T> ruleType) {
        AssertUtil.notEmpty(homePage, "Bad machine homePage");
        Map<String, String> params = null;
        if (StringUtil.isNotEmpty(type)) {
            params = new HashMap<>(1);
            params.put("type", type);
        }
        return executeCommand(homePage, api, params, false)
                .thenApply(json -> {
                	JsonObject jsonObject = JSONFormatter.fromJSON(json, JsonObject.class);
                	if(jsonObject == null || jsonObject.get("code") == null) {
                		return null;
                	}
                	JsonElement result = jsonObject.get("data");
                	if(result == null || !result.isJsonArray()) {
                		return null;
                	}
                	return JSONFormatter.fromList(result, ruleType);
                });
    }
    
    @Nullable
    private <T> List<T> fetchItems(String homePage, String api, String type, Class<T> ruleType) {
        try {
            AssertUtil.notEmpty(homePage, "Bad machine homePage");
            Map<String, String> params = null;
            if (StringUtil.isNotEmpty(type)) {
                params = new HashMap<>(1);
                params.put("type", type);
            }
            return fetchItemsAsync(homePage, api, type, ruleType).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error when fetching items from api: {} -> {}", api, type, e);
            return null;
        } catch (Exception e) {
            logger.error("Error when fetching items: {} -> {}", api, type, e);
            return null;
        }
    }
    
    private <T extends Rule> List<T> fetchRules(String homePage, String type, Class<T> ruleType) {
        return fetchItems(homePage, GET_RULES_PATH, type, ruleType);
    }
    
    private boolean setRules(String app, String homePage, String type, List<? extends RuleEntity> entities) {
        if (entities == null) {
            return true;
        }
        try {
            AssertUtil.notEmpty(app, "Bad app name");
            AssertUtil.notEmpty(homePage, "Bad machine homePage");
            String data = JSONFormatter.toJSON(
                    entities.stream().map(r -> r.toRule()).collect(Collectors.toList()));
            Map<String, String> params = new HashMap<>(2);
            params.put("type", type);
            params.put("data", data);
            String result = executeCommand(app, homePage, SET_RULES_PATH, params, true).get();
            logger.info("setRules result: {}, type={}", result, type);
            return true;
        } catch (InterruptedException e) {
            logger.warn("setRules API failed: {}", type, e);
            return false;
        } catch (ExecutionException e) {
            logger.warn("setRules API failed: {}", type, e.getCause());
            return false;
        } catch (Exception e) {
            logger.error("setRules API failed, type={}", type, e);
            return false;
        }
    }

    private CompletableFuture<Void> setRulesAsync(String app, String homePage, String type, List<? extends RuleEntity> entities) {
        try {
            AssertUtil.notNull(entities, "rules cannot be null");
            AssertUtil.notEmpty(app, "Bad app name");
            AssertUtil.notEmpty(homePage, "Bad machine homePage");
            String data = JSONFormatter.toJSON(
                entities.stream().map(r -> r.toRule()).collect(Collectors.toList()));
            Map<String, String> params = new HashMap<>(2);
            params.put("type", type);
            params.put("data", data);
            return executeCommand(app, homePage, SET_RULES_PATH, params, true)
                .thenCompose(r -> {
                	JsonObject jsonObject = JSONFormatter.fromJSON(r, JsonObject.class);
                	if(jsonObject == null || jsonObject.get("code") == null) {
                		return null;
                	}
                	String success = jsonObject.get("data").getAsString();
                	if (CommandConstants.MSG_SUCCESS.equals(success)) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return AsyncUtils.newFailedFuture(new CommandFailedException(r));
                });
        } catch (Exception e) {
            logger.error("setRulesAsync API failed, type={}", type, e);
            return AsyncUtils.newFailedFuture(e);
        }
    }

    public List<NodeVo> fetchResourceOfMachine(String homePage, String type) {
        return fetchItems(homePage, RESOURCE_URL_PATH, type, NodeVo.class);
    }

    /**
     * Fetch cluster node.
     *
     * @param ip          ip to fetch
     * @param port        port of the ip
     * @param includeZero whether zero value should in the result list.
     * @return
     */
    public List<NodeVo> fetchClusterNodeOfMachine(String homePage, boolean includeZero) {
        String type = "notZero";
        if (includeZero) {
            type = "zero";
        }
        return fetchItems(homePage, CLUSTER_NODE_PATH, type, NodeVo.class);
    }

    public List<FlowRuleEntity> fetchFlowRuleOfMachine(String app, String homePage) {
        List<FlowRule> rules = fetchRules(homePage, FLOW_RULE_TYPE, FlowRule.class);
        if (rules != null) {
            return rules.stream().map(rule -> FlowRuleEntity.fromFlowRule(app, rule))
                .collect(Collectors.toList());
        }
        return null;
    }

    public List<DegradeRuleEntity> fetchDegradeRuleOfMachine(String app, String homePage) {
        List<DegradeRule> rules = fetchRules(homePage, DEGRADE_RULE_TYPE, DegradeRule.class);
        if (rules != null) {
            return rules.stream().map(rule -> DegradeRuleEntity.fromDegradeRule(app, rule))
                .collect(Collectors.toList());
        }
        return null;
    }

    public List<SystemRuleEntity> fetchSystemRuleOfMachine(String app, String homePage) {
        List<SystemRule> rules = fetchRules(homePage, SYSTEM_RULE_TYPE, SystemRule.class);
        if (rules != null) {
            return rules.stream().map(rule -> SystemRuleEntity.fromSystemRule(app, rule))
                .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Fetch all parameter flow rules from provided machine.
     *
     * @param app  application name
     * @param ip   machine client IP
     * @param port machine client port
     * @return all retrieved parameter flow rules
     */
    public CompletableFuture<List<ParamFlowRuleEntity>> fetchParamFlowRulesOfMachine(String app, String homePage) {
        try {
            AssertUtil.notEmpty(app, "Bad app name");
            AssertUtil.notEmpty(homePage, "Bad machine homePage");
            return fetchItemsAsync(homePage, GET_PARAM_RULE_PATH, null, ParamFlowRule.class)
                .thenApply(rules -> {
                	return rules.stream()
                    .map(e -> ParamFlowRuleEntity.fromAuthorityRule(app, e))
                    .collect(Collectors.toList());
                });
        } catch (Exception e) {
            logger.error("Error when fetching parameter flow rules", e);
            return AsyncUtils.newFailedFuture(e);
        }
    }

    /**
     * Fetch all authority rules from provided machine.
     *
     * @param app  application name
     * @param ip   machine client IP
     * @param port machine client port
     * @return all retrieved authority rules
     */
    public List<AuthorityRuleEntity> fetchAuthorityRulesOfMachine(String app, String homePage) {
        AssertUtil.notEmpty(app, "Bad app name");
        AssertUtil.notEmpty(homePage, "Bad machine homePage");
        Map<String, String> params = new HashMap<>(1);
        params.put("type", AUTHORITY_TYPE);
        List<AuthorityRule> rules = fetchRules(homePage, AUTHORITY_TYPE, AuthorityRule.class);
        return Optional.ofNullable(rules).map(r -> r.stream()
                    .map(e -> AuthorityRuleEntity.fromAuthorityRule(app, e))
                    .collect(Collectors.toList())
                ).orElse(null);
    }

    /**
     * set rules of the machine. rules == null will return immediately;
     * rules.isEmpty() means setting the rules to empty.
     *
     * @param app
     * @param ip
     * @param port
     * @param rules
     * @return whether successfully set the rules.
     */
    public boolean setFlowRuleOfMachine(String app, String homePage, List<FlowRuleEntity> rules) {
        return setRules(app, homePage, FLOW_RULE_TYPE, rules);
    }

    public CompletableFuture<Void> setFlowRuleOfMachineAsync(String app, String homePage, List<FlowRuleEntity> rules) {
        return setRulesAsync(app, homePage, FLOW_RULE_TYPE, rules);
    }

    /**
     * set rules of the machine. rules == null will return immediately;
     * rules.isEmpty() means setting the rules to empty.
     *
     * @param app
     * @param ip
     * @param port
     * @param rules
     * @return whether successfully set the rules.
     */
    public boolean setDegradeRuleOfMachine(String app, String homePage, List<DegradeRuleEntity> rules) {
        return setRules(app, homePage, DEGRADE_RULE_TYPE, rules);
    }

    /**
     * set rules of the machine. rules == null will return immediately;
     * rules.isEmpty() means setting the rules to empty.
     *
     * @param app
     * @param ip
     * @param port
     * @param rules
     * @return whether successfully set the rules.
     */
    public boolean setSystemRuleOfMachine(String app, String homePage, List<SystemRuleEntity> rules) {
        return setRules(app, homePage, SYSTEM_RULE_TYPE, rules);
    }

    public boolean setAuthorityRuleOfMachine(String app, String homePage, List<AuthorityRuleEntity> rules) {
        return setRules(app, homePage, AUTHORITY_TYPE, rules);
    }

    public CompletableFuture<Void> setParamFlowRuleOfMachine(String app, String homePage, List<ParamFlowRuleEntity> rules) {
        if (rules == null) {
            return CompletableFuture.completedFuture(null);
        }
        if (StringUtil.isBlank(homePage)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            String data = JSONFormatter.toJSON(
                rules.stream().map(ParamFlowRuleEntity::getRule).collect(Collectors.toList())
            );
            Map<String, String> params = new HashMap<>(1);
            params.put("data", data);
            return executeCommand(app, homePage, SET_PARAM_RULE_PATH, params, true)
                .thenCompose(e -> {
                	JsonObject jsonObject = JSONFormatter.fromJSON(e, JsonObject.class);
                	if(jsonObject == null || jsonObject.get("code") == null) {
                		return null;
                	}
                	String success = jsonObject.get("data").getAsString();
                	if (CommandConstants.MSG_SUCCESS.equals(success)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Push parameter flow rules to client failed: " + e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when setting parameter flow rule", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    // Cluster related

    public CompletableFuture<ClusterStateSimpleEntity> fetchClusterMode(String homePage) {
        if (StringUtil.isBlank(homePage)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            return executeCommand(homePage, FETCH_CLUSTER_MODE_PATH, false)
                .thenApply(r -> {
                	JsonObject jsonObject = JSONFormatter.fromJSON(r, JsonObject.class);
                	if(jsonObject == null || jsonObject.get("code") == null) {
                		return null;
                	}
                	JsonElement result = jsonObject.get("data");
                	if(result == null || !result.isJsonObject()) {
                		return null;
                	}
                	return JSONFormatter.fromJSON(result, ClusterStateSimpleEntity.class);
                });
        } catch (Exception ex) {
            logger.warn("Error when fetching cluster mode", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterMode(String homePage, int mode) {
        if (StringUtil.isBlank(homePage)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            Map<String, String> params = new HashMap<>(1);
            params.put("mode", String.valueOf(mode));
            return executeCommand(homePage, MODIFY_CLUSTER_MODE_PATH, params, false)
                .thenCompose(e -> {
                	JsonObject jsonObject = JSONFormatter.fromJSON(e, JsonObject.class);
                	if(jsonObject == null || jsonObject.get("code") == null) {
                		return null;
                	}
                	String success = jsonObject.get("data").getAsString();
                	if (CommandConstants.MSG_SUCCESS.equals(success)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster mode: " + e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster mode", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<ClusterClientInfoVO> fetchClusterClientInfoAndConfig(String homePage) {
        if (StringUtil.isBlank(homePage)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            return executeCommand(homePage, FETCH_CLUSTER_CLIENT_CONFIG_PATH, false)
                .thenApply(r -> {
                	JsonObject jsonObject = JSONFormatter.fromJSON(r, JsonObject.class);
                	if(jsonObject == null || jsonObject.get("code") == null) {
                		return null;
                	}
                	JsonElement result = jsonObject.get("data");
                	if(result == null || !result.isJsonObject()) {
                		return null;
                	}
                	return JSONFormatter.fromJSON(result, ClusterClientInfoVO.class);
                });
        } catch (Exception ex) {
            logger.warn("Error when fetching cluster client config", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterClientConfig(String app, String homePage, ClusterClientConfig config) {
        if (StringUtil.isBlank(homePage)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            Map<String, String> params = new HashMap<>(1);
            params.put("data", JSONFormatter.toJSON(config));
            return executeCommand(app, homePage, MODIFY_CLUSTER_CLIENT_CONFIG_PATH, params, true)
                .thenCompose(e -> {
                	JsonObject jsonObject = JSONFormatter.fromJSON(e, JsonObject.class);
                	if(jsonObject == null || jsonObject.get("code") == null) {
                		return null;
                	}
                	String success = jsonObject.get("data").getAsString();
                	if (CommandConstants.MSG_SUCCESS.equals(success)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster client config: " + e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster client config", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterServerFlowConfig(String app, String homePage, ServerFlowConfig config) {
        if (StringUtil.isBlank(homePage)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            Map<String, String> params = new HashMap<>(1);
            params.put("data", JSONFormatter.toJSON(config));
            return executeCommand(app, homePage, MODIFY_CLUSTER_SERVER_FLOW_CONFIG_PATH, params, true)
                .thenCompose(e -> {
                	JsonObject jsonObject = JSONFormatter.fromJSON(e, JsonObject.class);
                	if(jsonObject == null || jsonObject.get("code") == null) {
                		return null;
                	}
                	String success = jsonObject.get("data").getAsString();
                	if (CommandConstants.MSG_SUCCESS.equals(success)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster server flow config: " + e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster server flow config", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterServerTransportConfig(String app, String homePage, ServerTransportConfig config) {
        if (StringUtil.isBlank(homePage)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            Map<String, String> params = new HashMap<>(2);
            params.put("port", config.getPort().toString());
            params.put("idleSeconds", config.getIdleSeconds().toString());
            return executeCommand(app, homePage, MODIFY_CLUSTER_SERVER_TRANSPORT_CONFIG_PATH, params, false)
                .thenCompose(e -> {
                	JsonObject jsonObject = JSONFormatter.fromJSON(e, JsonObject.class);
                	if(jsonObject == null || jsonObject.get("code") == null) {
                		return null;
                	}
                	String success = jsonObject.get("data").getAsString();
                	if (CommandConstants.MSG_SUCCESS.equals(success)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster server transport config: " + e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster server transport config", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterServerNamespaceSet(String app, String homePage, Set<String> set) {
        if (StringUtil.isBlank(homePage)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            Map<String, String> params = new HashMap<>(1);
            params.put("data", JSONFormatter.toJSON(set));
            return executeCommand(app, homePage, MODIFY_CLUSTER_SERVER_NAMESPACE_SET_PATH, params, true)
                .thenCompose(e -> {
                	JsonObject jsonObject = JSONFormatter.fromJSON(e, JsonObject.class);
                	if(jsonObject == null || jsonObject.get("code") == null) {
                		return null;
                	}
                	String success = jsonObject.get("data").getAsString();
                	if (CommandConstants.MSG_SUCCESS.equals(success)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster server NamespaceSet", e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster server NamespaceSet", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<ClusterServerStateVO> fetchClusterServerBasicInfo(String homePage) {
        if (StringUtil.isBlank(homePage)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            return executeCommand(homePage, FETCH_CLUSTER_SERVER_BASIC_INFO_PATH, false)
                .thenApply(r -> {
                	JsonObject jsonObject = JSONFormatter.fromJSON(r, JsonObject.class);
                	if(jsonObject == null || jsonObject.get("code") == null) {
                		return null;
                	}
                	JsonElement result = jsonObject.get("data");
                	if(result == null || !result.isJsonObject()) {
                		return null;
                	}
                	return JSONFormatter.fromJSON(result, ClusterServerStateVO.class);
                });
        } catch (Exception ex) {
            logger.warn("Error when fetching cluster sever all config and basic info", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<List<ApiDefinitionEntity>> fetchApis(String app, String homePage) {
        if (StringUtil.isBlank(homePage)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }

        try {
            return executeCommand(homePage, FETCH_GATEWAY_API_PATH, false)
                    .thenApply(r -> {
                    	JsonObject jsonObject = JSONFormatter.fromJSON(r, JsonObject.class);
                    	if(jsonObject == null || jsonObject.get("code") == null) {
                    		return null;
                    	}
                    	JsonElement result = jsonObject.get("data");
                    	if(result == null || !result.isJsonArray()) {
                    		return null;
                    	}
                        List<ApiDefinitionEntity> entities = JSONFormatter.fromList(result, ApiDefinitionEntity.class);
                        if (entities != null) {
                            for (ApiDefinitionEntity entity : entities) {
                                entity.setApp(app);
                            }
                        }
                        return entities;
                    });
        } catch (Exception ex) {
            logger.warn("Error when fetching gateway apis", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public boolean modifyApis(String app, String homePage, List<ApiDefinitionEntity> apis) {
        if (apis == null) {
            return true;
        }

        try {
            AssertUtil.notEmpty(app, "Bad app name");
            AssertUtil.notEmpty(homePage, "Bad machine homePage");
            String data = JSONFormatter.toJSON(
                    apis.stream().map(r -> r.toApiDefinition()).collect(Collectors.toList()));
            Map<String, String> params = new HashMap<>(2);
            params.put("data", data);
            String result = executeCommand(app, homePage, MODIFY_GATEWAY_API_PATH, params, true).get();
            logger.info("Modify gateway apis: {}", result);
            return true;
        } catch (Exception e) {
            logger.warn("Error when modifying gateway apis", e);
            return false;
        }
    }

    public CompletableFuture<List<GatewayFlowRuleEntity>> fetchGatewayFlowRules(String app, String homePage) {
        if (StringUtil.isBlank(homePage)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }

        try {
            return executeCommand(homePage, FETCH_GATEWAY_FLOW_RULE_PATH, false)
                    .thenApply(r -> {
                    	JsonObject jsonObject = JSONFormatter.fromJSON(r, JsonObject.class);
                    	if(jsonObject == null || jsonObject.get("code") == null) {
                    		return null;
                    	}
                    	JsonElement result = jsonObject.get("data");
                    	if(result == null || !result.isJsonArray()) {
                    		return null;
                    	}
                    	List<GatewayFlowRuleEntity> entities = null;
                        List<GatewayFlowRule> gatewayFlowRules = JSONFormatter.fromList(result, GatewayFlowRule.class);
                        if(gatewayFlowRules != null) {
                        	entities = gatewayFlowRules.stream().map(rule -> GatewayFlowRuleEntity.fromGatewayFlowRule(app, rule)).collect(Collectors.toList());
                        }
                        return entities;
                    });
        } catch (Exception ex) {
            logger.warn("Error when fetching gateway flow rules", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public boolean modifyGatewayFlowRules(String app, String homePage, List<GatewayFlowRuleEntity> rules) {
        if (rules == null) {
            return true;
        }

        try {
            AssertUtil.notEmpty(app, "Bad app name");
            AssertUtil.notEmpty(homePage, "Bad machine homePage");
            String data = JSONFormatter.toJSON(
                    rules.stream().map(r -> r.toGatewayFlowRule()).collect(Collectors.toList()));
            Map<String, String> params = new HashMap<>(2);
            params.put("data", data);
            String result = executeCommand(app, homePage, MODIFY_GATEWAY_FLOW_RULE_PATH, params, true).get();
            logger.info("Modify gateway flow rules: {}", result);
            return true;
        } catch (Exception e) {
            logger.warn("Error when modifying gateway apis", e);
            return false;
        }
    }
    
    public class EncodedJsonEntity extends StringEntity {

		public EncodedJsonEntity(final Map<String, String> params, final Charset charset) {
			super(JSONFormatter.toJSON(params), charset != null ? charset : HTTP.DEF_CONTENT_CHARSET);
		}
    	
    }
}
