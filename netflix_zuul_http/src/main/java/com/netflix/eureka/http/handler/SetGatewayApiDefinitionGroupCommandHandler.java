package com.netflix.eureka.http.handler;

import java.net.URLDecoder;
import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;

import static com.netflix.eureka.transport.util.WritableDataSourceRegistry.getApiDefinitionWds;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.common.ApiDefinition;
import com.netflix.eureka.common.ApiPathPredicateItem;
import com.netflix.eureka.datasource.WritableDataSource;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.http.api.zuul.GatewayApiDefinitionManager;
import com.netflix.eureka.http.cache.IRouteCache;

@Endpoint(id = "setApiDefinitions")
public class SetGatewayApiDefinitionGroupCommandHandler {

	private IRouteCache zuulCache;
	
	public SetGatewayApiDefinitionGroupCommandHandler(IRouteCache zuulCache){
		this.zuulCache = zuulCache;
	}
	
    @WriteOperation
    public CommandResponse<String> handle(String data) {
        try {
            data = URLDecoder.decode(data, "utf-8");
        } catch (Exception e) {
            RecordLog.info("Decode gateway API definition data error", e);
            return CommandResponse.ofFailure("decode gateway API definition data error");
        }

        RecordLog.info("[API Server] Receiving data change (type: gateway API definition): {}", data);

        String result = SUCCESS_MSG;

        List<ApiDefinition> apiDefinitions = JSONFormatter.fromList(data, ApiDefinition.class);
        GatewayApiDefinitionManager.loadApiDefinitions(apiDefinitions);
        if (writeToDataSource(getApiDefinitionWds(), apiDefinitions)) {
        	for(ApiDefinition api: apiDefinitions) {
        		zuulCache.addRoute(zuulRoute(api));
        	}
        } else {
        	result = WRITE_DS_FAILURE_MSG;
        }
        return CommandResponse.ofSuccess(result);
    }

    private static final String SUCCESS_MSG = "success";
    private static final String WRITE_DS_FAILURE_MSG = "partial success (write data source failed)";

    private ZuulRoute zuulRoute(ApiDefinition api) {
    	ApiPathPredicateItem pathPredicate = api.getPredicateItems();
    	if(pathPredicate == null || StringUtil.isEmpty(pathPredicate.getPattern())) {
    		return null;
    	}
    	
    	ZuulRoute zuul = new ZuulRoute();
		zuul.setId(api.getApiName());
    	zuul.setServiceId(api.getServiceId());
    	zuul.setStripPrefix(true);
    	
//    	switch (pathPredicate.getMatchStrategy()) {
//        case CommandConstants.URL_MATCH_STRATEGY_REGEX:
//        	zuul.setPath(pathPredicate.getPattern());
//        case CommandConstants.URL_MATCH_STRATEGY_PREFIX:
//        	zuul.setPath(pathPredicate.getPattern());
//        default:
//        	zuul.setPath(pathPredicate.getPattern());
//    	}
    	zuul.setPath(pathPredicate.getPattern());
    	return zuul;
	}
    
    /**
     * Write target value to given data source.
     *
     * @param dataSource writable data source
     * @param value target value to save
     * @param <T> value type
     * @return true if write successful or data source is empty; false if error occurs
     */
    private <T> boolean writeToDataSource(WritableDataSource<T> dataSource, T value) {
        if (dataSource != null) {
            try {
                dataSource.write(value);
            } catch (Exception e) {
                RecordLog.warn("Write data source failed", e);
                return false;
            }
        }
        return true;
    }

}
