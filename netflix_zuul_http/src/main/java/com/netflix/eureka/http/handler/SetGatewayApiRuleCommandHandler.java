package com.netflix.eureka.http.handler;

import static com.netflix.eureka.transport.util.WritableDataSourceRegistry.getGatewayFlowWds;

import java.net.URLDecoder;
import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.common.GatewayFlowRule;
import com.netflix.eureka.datasource.WritableDataSource;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.http.common.rule.GatewayRuleManager;

/**
 * Update gateway rules
 * @author WX
 *
 */
@Endpoint(id = "setApiRules")
public class SetGatewayApiRuleCommandHandler {

    @WriteOperation
    public CommandResponse<String> handle(String data) {
        try {
            data = URLDecoder.decode(data, "utf-8");
        } catch (Exception e) {
            RecordLog.info("Decode gateway rule data error", e);
            return CommandResponse.ofFailure("decode gateway rule data error");
        }

        RecordLog.info("[API Server] Receiving rule change (type: gateway rule): {}", data);

        String result = SUCCESS_MSG;
	    List<GatewayFlowRule> flowRules = JSONFormatter.fromList(data, GatewayFlowRule.class);
        GatewayRuleManager.loadRules(flowRules);
        if (!writeToDataSource(getGatewayFlowWds(), flowRules)) {
            result = WRITE_DS_FAILURE_MSG;
        }
        return CommandResponse.ofSuccess(result);
    }

    /**
     * Write target value to given data source.
     *
     * @param dataSource writable data source
     * @param value target value to save
     * @param <T> value type
     * @return true if write successful or data source is empty; false if error occurs
     */
    private <T> boolean writeToDataSource(WritableDataSource<T> dataSource, T value) {
        if (dataSource != null) {
            try {
                dataSource.write(value);
            } catch (Exception e) {
                RecordLog.warn("Write data source failed", e);
                return false;
            }
        }
        return true;
    }

    private static final String SUCCESS_MSG = "success";
    private static final String WRITE_DS_FAILURE_MSG = "partial success (write data source failed)";
}
