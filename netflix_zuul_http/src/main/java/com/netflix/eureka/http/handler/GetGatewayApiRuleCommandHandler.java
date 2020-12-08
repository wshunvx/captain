package com.netflix.eureka.http.handler;

import java.util.Set;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.common.GatewayFlowRule;
import com.netflix.eureka.http.common.rule.GatewayRuleManager;

/**
 * Fetch all gateway rules
 * @author WX
 *
 */
@Endpoint(id = "getApiRules")
public class GetGatewayApiRuleCommandHandler {

	@ReadOperation
    public CommandResponse<Set<GatewayFlowRule>> handle() {
        return CommandResponse.ofSuccess(GatewayRuleManager.getRules());
    }
}
