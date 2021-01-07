package com.netflix.eureka.http.handler;

import static com.netflix.eureka.transport.util.WritableDataSourceRegistry.getAuthorityDataSource;
import static com.netflix.eureka.transport.util.WritableDataSourceRegistry.getDegradeDataSource;
import static com.netflix.eureka.transport.util.WritableDataSourceRegistry.getFlowDataSource;
import static com.netflix.eureka.transport.util.WritableDataSourceRegistry.getSystemSource;

import java.net.URLDecoder;
import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.lang.Nullable;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.eureka.command.CommandConstants;
import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.datasource.WritableDataSource;
import com.netflix.eureka.gson.JSONFormatter;

/**
 * modify the rules, accept param: type={ruleType}&data={ruleJson}
 * @author WX
 *
 */
@Endpoint(id = "setRules")
public class ModifyRulesCommandHandler {

    @WriteOperation
    public CommandResponse<String> handle(String type, @Nullable String data) {
        // rule data in get parameter
        if (StringUtil.isNotEmpty(data)) {
            try {
                data = URLDecoder.decode(data, "utf-8");
            } catch (Exception e) {
                RecordLog.info("Decode rule data error", e);
                return CommandResponse.ofFailure("decode rule data error");
            }
        }
        RecordLog.info("Receiving rule change (type: {}): {}", type, data);

        String result = CommandConstants.MSG_SUCCESS;
        if (FLOW_RULE_TYPE.equalsIgnoreCase(type)) {
        	List<FlowRule> rules = JSONFormatter.fromList(data, FlowRule.class);
        	if(rules != null) {
        		FlowRuleManager.loadRules(rules);
                if (!writeToDataSource(getFlowDataSource(), rules)) {
                    result = WRITE_DS_FAILURE_MSG;
                }
        	}
            return CommandResponse.ofSuccess(result);
        } else if (AUTHORITY_RULE_TYPE.equalsIgnoreCase(type)) {
            List<AuthorityRule> rules = JSONFormatter.fromList(data, AuthorityRule.class);
            if(rules != null) {
            	AuthorityRuleManager.loadRules(rules);
                if (!writeToDataSource(getAuthorityDataSource(), rules)) {
                    result = WRITE_DS_FAILURE_MSG;
                }
            }
            return CommandResponse.ofSuccess(result);
        } else if (DEGRADE_RULE_TYPE.equalsIgnoreCase(type)) {
            List<DegradeRule> rules = JSONFormatter.fromList(data, DegradeRule.class);
            if(rules != null) {
            	DegradeRuleManager.loadRules(rules);
                if (!writeToDataSource(getDegradeDataSource(), rules)) {
                    result = WRITE_DS_FAILURE_MSG;
                }
            }
            return CommandResponse.ofSuccess(result);
        } else if (SYSTEM_RULE_TYPE.equalsIgnoreCase(type)) {
            List<SystemRule> rules = JSONFormatter.fromList(data, SystemRule.class);
            if(rules != null) {
            	SystemRuleManager.loadRules(rules);
                if (!writeToDataSource(getSystemSource(), rules)) {
                    result = WRITE_DS_FAILURE_MSG;
                }
            }
            return CommandResponse.ofSuccess(result);
        }
        return CommandResponse.ofFailure("invalid type");
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

    private static final String WRITE_DS_FAILURE_MSG = "partial success (write data source failed)";
    private static final String FLOW_RULE_TYPE = "flow";
    private static final String DEGRADE_RULE_TYPE = "degrade";
    private static final String SYSTEM_RULE_TYPE = "system";
    private static final String AUTHORITY_RULE_TYPE = "authority";
}
