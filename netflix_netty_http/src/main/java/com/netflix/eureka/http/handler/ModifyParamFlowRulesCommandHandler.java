package com.netflix.eureka.http.handler;

import java.net.URLDecoder;
import java.util.List;

import static com.netflix.eureka.transport.util.WritableDataSourceRegistry.getParamFlowWds;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.common.ParamFlowRule;
import com.netflix.eureka.datasource.WritableDataSource;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.http.slots.block.ParamFlowRuleManager;

/**
 * Set parameter flow rules, while previous rules will be replaced.
 * @author WX
 *
 */
@Endpoint(id = "setParamFlowRules")
public class ModifyParamFlowRulesCommandHandler {

    @WriteOperation
    public CommandResponse<String> handle(String data) {
        try {
            data = URLDecoder.decode(data, "utf-8");
        } catch (Exception e) {
            RecordLog.info("Decode rule data error", e);
            return CommandResponse.ofFailure("decode rule data error");
        }

        RecordLog.info("[API Server] Receiving rule change (type:parameter flow rule): {}", data);

        String result = SUCCESS_MSG;
        List<ParamFlowRule> flowRules = JSONFormatter.fromList(data, ParamFlowRule.class);
        ParamFlowRuleManager.loadRules(flowRules);
        if (!writeToDataSource(getParamFlowWds(), flowRules)) {
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
