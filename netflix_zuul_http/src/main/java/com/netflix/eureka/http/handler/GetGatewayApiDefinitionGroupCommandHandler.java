package com.netflix.eureka.http.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.netflix.eureka.command.CommandConstants;
import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.common.ApiDefinition;
import com.netflix.eureka.http.cache.IRouteCache;

/**
 * Fetch all customized gateway API groups
 * @author WX
 *
 */
@Endpoint(id = "getApiDefinitions")
public class GetGatewayApiDefinitionGroupCommandHandler {

	private IRouteCache routeCache;
	
	public GetGatewayApiDefinitionGroupCommandHandler(IRouteCache routeCache){
		this.routeCache = routeCache;
	}
	
	@ReadOperation
    public CommandResponse<List<ApiDefinition>> handle() {
		List<ApiDefinition> apis = new ArrayList<>();
		routeCache.getLocateRoutes().forEach((path, route) -> {
    		ApiDefinition definition = new ApiDefinition();
			definition.setApiName(route.getId());
			definition.setServiceId(route.getServiceId());
			definition.setUrl(route.getUrl());
			definition.setPattern(route.getPath());
			definition.setStripPrefix(route.isStripPrefix() ? CommandConstants.STRIP_PREFIX_ROUTE_TRUE : CommandConstants.STRIP_PREFIX_ROUTE_FALSE);
			definition.setMatchStrategy(CommandConstants.URL_MATCH_STRATEGY_PREFIX);
			
			apis.add(definition);
    	});
        return CommandResponse.ofSuccess(apis);
    }
}