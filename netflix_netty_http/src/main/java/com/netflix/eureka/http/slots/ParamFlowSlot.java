package com.netflix.eureka.http.slots;

import java.util.List;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.spi.SpiOrder;
import com.netflix.eureka.common.ParamFlowRule;
import com.netflix.eureka.http.slots.block.ParamFlowChecker;
import com.netflix.eureka.http.slots.block.ParamFlowRuleManager;
import com.netflix.eureka.http.slots.block.ParameterMetricStorage;

@SpiOrder(-1)
public class ParamFlowSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count,
                      boolean prioritized, Object... args) throws Throwable {
        if (!ParamFlowRuleManager.hasRules(resourceWrapper.getName())) {
            fireEntry(context, resourceWrapper, node, count, prioritized, args);
            return;
        }

        checkFlow(resourceWrapper, count, args);
        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        fireExit(context, resourceWrapper, count, args);
    }

    void applyRealParamIdx(/*@NonNull*/ ParamFlowRule rule, int length) {
        int paramIdx = rule.getParamIdx();
        if (paramIdx < 0) {
            if (-paramIdx <= length) {
                rule.setParamIdx(length + paramIdx);
            } else {
                // Illegal index, give it a illegal positive value, latter rule checking will pass.
                rule.setParamIdx(-paramIdx);
            }
        }
    }

    void checkFlow(ResourceWrapper resourceWrapper, int count, Object... args) throws BlockException {
        if (args == null) {
            return;
        }
        if (!ParamFlowRuleManager.hasRules(resourceWrapper.getName())) {
            return;
        }
        List<ParamFlowRule> rules = ParamFlowRuleManager.getRulesOfResource(resourceWrapper.getName());

        for (ParamFlowRule rule : rules) {
            applyRealParamIdx(rule, args.length);

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
}
