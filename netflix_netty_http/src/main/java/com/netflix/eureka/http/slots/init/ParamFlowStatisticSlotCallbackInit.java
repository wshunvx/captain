package com.netflix.eureka.http.slots.init;

import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlotCallbackRegistry;
import com.netflix.eureka.http.slots.statistic.ParamFlowStatisticEntryCallback;
import com.netflix.eureka.http.slots.statistic.ParamFlowStatisticExitCallback;

public class ParamFlowStatisticSlotCallbackInit implements InitFunc {

    @Override
    public void init() {
        StatisticSlotCallbackRegistry.addEntryCallback(ParamFlowStatisticEntryCallback.class.getName(),
            new ParamFlowStatisticEntryCallback());
        StatisticSlotCallbackRegistry.addExitCallback(ParamFlowStatisticExitCallback.class.getName(),
            new ParamFlowStatisticExitCallback());
    }
}
