package com.netflix.eureka.http.slot;

import java.util.List;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.spi.SpiOrder;
import com.netflix.eureka.common.ParamFlowRule;
import com.netflix.eureka.http.common.rule.GatewayRuleManager;
import com.netflix.eureka.http.slots.block.ParamFlowChecker;
import com.netflix.eureka.http.slots.block.ParameterMetricStorage;

@SpiOrder(-1)
public class GatewayFlowSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

	@Override
    public void entry(Context context, ResourceWrapper resource, DefaultNode node, int count,
                      boolean prioritized, Object... args) throws Throwable {
        checkGatewayParamFlow(resource, count, args);

        fireEntry(context, resource, node, count, prioritized, args);
    }

    private void checkGatewayParamFlow(ResourceWrapper resourceWrapper, int count, Object... args) throws BlockException {
        if (args == null) {
            return;
        }

        List<ParamFlowRule> rules = GatewayRuleManager.getConvertedParamRules(resourceWrapper.getName());
        if (rules == null || rules.isEmpty()) {
            return;
        }

        for (ParamFlowRule rule : rules) {
            // Initialize the parameter metrics.
            ParameterMetricStorage.initParamMetricsFor(resourceWrapper, rule);

            if (!ParamFlowChecker.passCheck(resourceWrapper, rule, count, args)) {
                String triggeredParam = "";
                if (args.length > rule.getParamIdx()) {
                    Object value = args[rule.getParamIdx()];
                    triggeredParam = String.valueOf(value);
                }
                throw new BlockException(resourceWrapper.getName(), triggeredParam, rule) {

					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;
				};
            }
        }
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        fireExit(context, resourceWrapper, count, args);
    }
}
