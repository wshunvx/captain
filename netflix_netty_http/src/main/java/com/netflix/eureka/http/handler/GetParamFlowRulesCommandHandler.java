package com.netflix.eureka.http.handler;

import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.common.ParamFlowRule;
import com.netflix.eureka.http.slots.block.ParamFlowRuleManager;


/**
 * Get all parameter flow rules
 * @author WX
 *
 */
@Endpoint(id = "getParamFlowRules")
public class GetParamFlowRulesCommandHandler {

	@ReadOperation
    public CommandResponse<List<ParamFlowRule>> handle() {
        return CommandResponse.ofSuccess(ParamFlowRuleManager.getRules());
    }
}
