package com.netflix.eureka.http.common.rule;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.netflix.eureka.command.CommandConstants;
import com.netflix.eureka.common.GatewayFlowRule;
import com.netflix.eureka.common.GatewayParamFlowItem;
import com.netflix.eureka.common.ParamFlowItem;
import com.netflix.eureka.common.ParamFlowRule;

final class GatewayRuleConverter {

    static FlowRule toFlowRule(/*@Valid*/ GatewayFlowRule rule) {
        return new FlowRule(rule.getResource())
            .setControlBehavior(rule.getControlBehavior())
            .setCount(rule.getCount())
            .setGrade(rule.getGrade())
            .setMaxQueueingTimeMs(rule.getMaxQueueingTimeoutMs());
    }

    static ParamFlowItem generateNonMatchPassParamItem() {
        return new ParamFlowItem().setClassType(String.class.getName())
            .setCount(1000_0000)
            .setObject(CommandConstants.GATEWAY_NOT_MATCH_PARAM);
    }

    static ParamFlowItem generateNonMatchBlockParamItem() {
        return new ParamFlowItem().setClassType(String.class.getName())
            .setCount(0)
            .setObject(CommandConstants.GATEWAY_NOT_MATCH_PARAM);
    }

    static ParamFlowRule applyNonParamToParamRule(/*@Valid*/ GatewayFlowRule gatewayRule, int idx) {
        return new ParamFlowRule(gatewayRule.getResource())
            .setCount(gatewayRule.getCount())
            .setGrade(gatewayRule.getGrade())
            .setDurationInSec(gatewayRule.getIntervalSec())
            .setBurstCount(gatewayRule.getBurst())
            .setControlBehavior(gatewayRule.getControlBehavior())
            .setMaxQueueingTimeMs(gatewayRule.getMaxQueueingTimeoutMs())
            .setParamIdx(idx);
    }

    /**
     * Convert a gateway rule to parameter flow rule, then apply the generated
     * parameter index to {@link GatewayParamFlowItem} of the rule.
     *
     * @param gatewayRule a valid gateway rule that should contain valid parameter items
     * @param idx generated parameter index (callers should guarantee it's unique and incremental)
     * @return converted parameter flow rule
     */
    static ParamFlowRule applyToParamRule(/*@Valid*/ GatewayFlowRule gatewayRule, int idx) {
        ParamFlowRule paramRule = new ParamFlowRule(gatewayRule.getResource())
            .setCount(gatewayRule.getCount())
            .setGrade(gatewayRule.getGrade())
            .setDurationInSec(gatewayRule.getIntervalSec())
            .setBurstCount(gatewayRule.getBurst())
            .setControlBehavior(gatewayRule.getControlBehavior())
            .setMaxQueueingTimeMs(gatewayRule.getMaxQueueingTimeoutMs())
            .setParamIdx(idx);
        GatewayParamFlowItem gatewayItem = gatewayRule.getParamItem();
        // Apply the current idx to gateway rule item.
        gatewayItem.setIndex(idx);
        // Apply for pattern-based parameters.
        String valuePattern = gatewayItem.getPattern();
        if (valuePattern != null) {
            paramRule.getParamFlowItemList().add(generateNonMatchPassParamItem());
        }
        return paramRule;
    }

    private GatewayRuleConverter() {}
}
