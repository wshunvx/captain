package com.netflix.eureka.http.handler;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.netflix.eureka.command.CommandResponse;

/**
 * get sentinel config info
 * @author WX
 *
 */
@Endpoint(id = "basicInfo")
public class BasicInfoCommandHandler {

	@ReadOperation
    public CommandResponse<String> handle() {
        return CommandResponse.ofSuccess(HostNameUtil.getConfigString());
    }
}
