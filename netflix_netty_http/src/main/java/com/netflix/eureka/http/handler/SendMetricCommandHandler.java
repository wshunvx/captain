package com.netflix.eureka.http.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.node.metric.MetricSearcher;
import com.alibaba.csp.sentinel.node.metric.MetricWriter;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.netflix.eureka.command.CommandResponse;

/**
 * get and aggregate metrics, accept param: 
   startTime={startTime}&endTime={endTime}&maxLines={maxLines}&identify={resourceName}
 * @author WX
 *
 */
@Endpoint(id = "metric")
public class SendMetricCommandHandler {

    private volatile MetricSearcher searcher;

    private final Object lock = new Object();

    @ReadOperation
    public CommandResponse<String> handle(String startTime, String endTime, String maxLines, String identity) {
        // Note: not thread-safe.
        if (searcher == null) {
            synchronized (lock) {
                String appName = SentinelConfig.getAppName();
                if (appName == null) {
                    appName = "";
                }
                if (searcher == null) {
                    searcher = new MetricSearcher(MetricWriter.METRIC_BASE_DIR,
                        MetricWriter.formMetricFileName(appName, PidUtil.getPid()));
                }
            }
        }
        long _startTime = -1;
        
        if (StringUtil.isNotBlank(startTime)) {
        	_startTime = Long.parseLong(startTime);
        } else {
            return CommandResponse.ofSuccess("");
        }
        
        int _maxLines = 6000;
        List<MetricNode> list;
        try {
            // Find by end time if set.
            if (StringUtil.isNotBlank(endTime)) {
                long _endTime = Long.parseLong(endTime);
                list = searcher.findByTimeAndResource(_startTime, _endTime, identity);
            } else {
                if (StringUtil.isNotBlank(maxLines)) {
                	_maxLines = Integer.parseInt(maxLines);
                }
                _maxLines = Math.min(_maxLines, 12000);
                list = searcher.find(_startTime, _maxLines);
            }
        } catch (Exception ex) {
            return CommandResponse.ofFailure("Error when retrieving metrics");
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        if (StringUtil.isBlank(identity)) {
            addCpuUsageAndLoad(list);
        }
        StringBuilder sb = new StringBuilder();
        for (MetricNode node : list) {
            sb.append(node.toThinString()).append("\n");
        }
        return CommandResponse.ofSuccess(sb.toString());
    }

    /**
     * add current cpu usage and load to the metric list.
     *
     * @param list metric list, should not be null
     */
    private void addCpuUsageAndLoad(List<MetricNode> list) {
        long time = TimeUtil.currentTimeMillis() / 1000 * 1000;
        double load = SystemRuleManager.getCurrentSystemAvgLoad();
        double usage = SystemRuleManager.getCurrentCpuUsage();
        if (load > 0) {
            MetricNode loadNode = toNode(load, time, Constants.SYSTEM_LOAD_RESOURCE_NAME);
            list.add(loadNode);
        }
        if (usage > 0) {
            MetricNode usageNode = toNode(usage, time, Constants.CPU_USAGE_RESOURCE_NAME);
            list.add(usageNode);
        }
    }

    /**
     * transfer the value to a MetricNode, the value will multiply 10000 then truncate
     * to long value, and as the {@link MetricNode#passQps}.
     * <p>
     * This is an eclectic scheme before we have a standard metric format.
     * </p>
     *
     * @param value    value to save.
     * @param ts       timestamp
     * @param resource resource name.
     * @return a MetricNode represents the value.
     */
    private MetricNode toNode(double value, long ts, String resource) {
        MetricNode node = new MetricNode();
        node.setPassQps((long)(value * 10000));
        node.setTimestamp(ts);
        node.setResource(resource);
        return node;
    }
}
