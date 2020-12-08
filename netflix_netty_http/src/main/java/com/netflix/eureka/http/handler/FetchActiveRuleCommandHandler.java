package com.netflix.eureka.http.handler;

import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.netflix.eureka.command.CommandResponse;

/**
 * get all active rules by type, request param: type={ruleType}
 * @author WX
 *
 */
@Endpoint(id = "getRules")
public class FetchActiveRuleCommandHandler {

    @ReadOperation
    public CommandResponse<List<? extends AbstractRule>> handle(String type) {
        if ("flow".equalsIgnoreCase(type)) {
            return CommandResponse.ofSuccess(FlowRuleManager.getRules());
        } else if ("degrade".equalsIgnoreCase(type)) {
            return CommandResponse.ofSuccess(DegradeRuleManager.getRules());
        } else if ("authority".equalsIgnoreCase(type)) {
            return CommandResponse.ofSuccess(AuthorityRuleManager.getRules());
        } else if ("system".equalsIgnoreCase(type)) {
            return CommandResponse.ofSuccess(SystemRuleManager.getRules());
        } else {
            return CommandResponse.ofFailure("invalid type");
        }
    }

}
