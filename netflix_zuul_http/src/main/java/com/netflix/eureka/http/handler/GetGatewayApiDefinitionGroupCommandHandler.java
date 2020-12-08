package com.netflix.eureka.http.handler;

import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.common.ApiDefinition;
import com.netflix.eureka.http.api.zuul.GatewayApiDefinitionManager;

/**
 * Fetch all customized gateway API groups
 * @author WX
 *
 */
@Endpoint(id = "getApiDefinitions")
public class GetGatewayApiDefinitionGroupCommandHandler {

	@ReadOperation
    public CommandResponse<List<ApiDefinition>> handle() {
        return CommandResponse.ofSuccess(GatewayApiDefinitionManager.getApiDefinitions());
    }
}