package com.netflix.eureka.http.handler.cluster;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.TokenClientProvider;
import com.alibaba.csp.sentinel.cluster.server.EmbeddedClusterTokenServerProvider;
import com.google.gson.JsonObject;
import com.netflix.eureka.command.CommandResponse;

/**
 * get cluster mode status
 * @author WX
 *
 */
@Endpoint(id = "getClusterMode")
public class FetchClusterModeCommandHandler {

	@ReadOperation
    public CommandResponse<JsonObject> handle() {
        JsonObject res = new JsonObject();
        res.addProperty("mode", ClusterStateManager.getMode());
        res.addProperty("lastModified", ClusterStateManager.getLastModified());
        res.addProperty("clientAvailable", isClusterClientSpiAvailable());
        res.addProperty("serverAvailable", isClusterServerSpiAvailable());
        return CommandResponse.ofSuccess(res);
    }

    private boolean isClusterClientSpiAvailable() {
        return TokenClientProvider.getClient() != null;
    }

    private boolean isClusterServerSpiAvailable() {
        return EmbeddedClusterTokenServerProvider.getServer() != null;
    }
}
