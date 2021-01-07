package com.netflix.eureka.http.handler;

import static com.netflix.eureka.transport.util.WritableDataSourceRegistry.getApiDefinitionWds;

import java.net.URLDecoder;
import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.netflix.eureka.command.CommandConstants;
import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.common.ApiDefinition;
import com.netflix.eureka.datasource.WritableDataSource;
import com.netflix.eureka.gson.JSONFormatter;
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
        if (writeToDataSource(getApiDefinitionWds(), apiDefinitions)) {
        	for(ApiDefinition api: apiDefinitions) {
        		int status = api.getStatus();
        		if(status == 0 || status == 1) {
        			zuulCache.addRoute(zuulRoute(api));
        		}else {
        			zuulCache.delRoute(api.getPattern());
        		}
        	}
        } else {
        	result = WRITE_DS_FAILURE_MSG;
        }
        return CommandResponse.ofSuccess(result);
    }

    private static final String SUCCESS_MSG = "success";
    private static final String WRITE_DS_FAILURE_MSG = "partial success (write data source failed)";

    private ZuulRoute zuulRoute(ApiDefinition api) {
    	ZuulRoute zuul = new ZuulRoute();
		zuul.setId(api.getApiName());
		String serviceId = api.getServiceId();
		if(StringUtils.isEmpty(serviceId)) {
			zuul.setUrl(api.getUrl());
		} else {
			zuul.setServiceId(api.getServiceId());
		}
		zuul.setPath(api.getPattern());
//    	switch (pathPredicate.getMatchStrategy()) {
//        case CommandConstants.URL_MATCH_STRATEGY_REGEX:
//        	zuul.setPath(pathPredicate.getPattern());
//        case CommandConstants.URL_MATCH_STRATEGY_PREFIX:
//        	zuul.setPath(pathPredicate.getPattern());
//        default:
//        	zuul.setPath(pathPredicate.getPattern());
//    	}
    	zuul.setStripPrefix(api.getStripPrefix() == CommandConstants.STRIP_PREFIX_ROUTE_TRUE);
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
