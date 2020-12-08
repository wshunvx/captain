package com.netflix.eureka.http.handler;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.Constants;
import com.netflix.eureka.command.CommandResponse;

/**
 * get sentinel version
 * @author WX
 *
 */
@Endpoint(id = "version")
public class VersionCommandHandler {

    @ReadOperation
    public CommandResponse<String> handle() {
        return CommandResponse.ofSuccess(Constants.SENTINEL_VERSION);
    }
}
