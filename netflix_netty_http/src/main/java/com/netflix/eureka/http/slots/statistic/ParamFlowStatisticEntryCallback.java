package com.netflix.eureka.http.slots.statistic;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.netflix.eureka.http.slots.block.ParameterMetric;
import com.netflix.eureka.http.slots.block.ParameterMetricStorage;

public class ParamFlowStatisticEntryCallback implements ProcessorSlotEntryCallback<DefaultNode> {

    @Override
    public void onPass(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, Object... args) {
        // The "hot spot" parameter metric is present only if parameter flow rules for the resource exist.
        ParameterMetric parameterMetric = ParameterMetricStorage.getParamMetric(resourceWrapper);

        if (parameterMetric != null) {
            parameterMetric.addThreadCount(args);
        }
    }

    @Override
    public void onBlocked(BlockException ex, Context context, ResourceWrapper resourceWrapper, DefaultNode param,
                          int count, Object... args) {
        // Here we don't add block count here because checking the type of block exception can affect performance.
        // We add the block count when throwing the BlockException instead.
    }
}
