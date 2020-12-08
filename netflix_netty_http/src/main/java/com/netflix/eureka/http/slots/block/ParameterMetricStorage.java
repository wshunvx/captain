package com.netflix.eureka.http.slots.block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.eureka.common.ParamFlowRule;

public final class ParameterMetricStorage {

    private static final Map<String, ParameterMetric> metricsMap = new ConcurrentHashMap<>();

    /**
     * Lock for a specific resource.
     */
    private static final Object LOCK = new Object();

    /**
     * Init the parameter metric and index map for given resource.
     * Package-private for test.
     *
     * @param resourceWrapper resource to init
     * @param rule            relevant rule
     */
    public static void initParamMetricsFor(ResourceWrapper resourceWrapper, /*@Valid*/ ParamFlowRule rule) {
        if (resourceWrapper == null || resourceWrapper.getName() == null) {
            return;
        }
        String resourceName = resourceWrapper.getName();
        ParameterMetric metric;
        // Assume that the resource is valid.
        if ((metric = metricsMap.get(resourceName)) == null) {
            synchronized (LOCK) {
                if ((metric = metricsMap.get(resourceName)) == null) {
                    metric = new ParameterMetric();
                    metricsMap.put(resourceWrapper.getName(), metric);
                    RecordLog.info("[ParameterMetricStorage] Creating parameter metric for: {}", resourceWrapper.getName());
                }
            }
        }
        metric.initialize(rule);
    }

    public static ParameterMetric getParamMetric(ResourceWrapper resourceWrapper) {
        if (resourceWrapper == null || resourceWrapper.getName() == null) {
            return null;
        }
        return metricsMap.get(resourceWrapper.getName());
    }

    public static ParameterMetric getParamMetricForResource(String resourceName) {
        if (resourceName == null) {
            return null;
        }
        return metricsMap.get(resourceName);
    }

    public static void clearParamMetricForResource(String resourceName) {
        if (StringUtil.isBlank(resourceName)) {
            return;
        }
        metricsMap.remove(resourceName);
        RecordLog.info("[ParameterMetricStorage] Clearing parameter metric for: {}", resourceName);
    }

    static Map<String, ParameterMetric> getMetricsMap() {
        return metricsMap;
    }

    private ParameterMetricStorage() {}
}
