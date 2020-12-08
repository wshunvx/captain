package com.netflix.eureka.http.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.Constants;
import com.netflix.eureka.command.CommandResponse;

/**
 * get system status
 * @author WX
 *
 */
@Endpoint(id = "systemStatus")
public class FetchSystemStatusCommandHandler {

    @ReadOperation
    public CommandResponse<Map<String, Object>> handle() {

        Map<String, Object> systemStatus = new HashMap<String, Object>();

        systemStatus.put("rqps", Constants.ENTRY_NODE.successQps());
        systemStatus.put("qps", Constants.ENTRY_NODE.passQps());
        systemStatus.put("b", Constants.ENTRY_NODE.blockQps());
        systemStatus.put("r", Constants.ENTRY_NODE.avgRt());
        systemStatus.put("t", Constants.ENTRY_NODE.curThreadNum());

        return CommandResponse.ofSuccess(systemStatus);
    }
}
