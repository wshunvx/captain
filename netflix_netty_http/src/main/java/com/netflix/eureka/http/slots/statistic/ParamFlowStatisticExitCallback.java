package com.netflix.eureka.http.slots.statistic;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.netflix.eureka.http.slots.block.ParameterMetric;
import com.netflix.eureka.http.slots.block.ParameterMetricStorage;

public class ParamFlowStatisticExitCallback implements ProcessorSlotExitCallback {

    @Override
    public void onExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        if (context.getCurEntry().getBlockError() == null) {
            ParameterMetric parameterMetric = ParameterMetricStorage.getParamMetric(resourceWrapper);

            if (parameterMetric != null) {
                parameterMetric.decreaseThreadCount(args);
            }
        }
    }
}