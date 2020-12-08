package com.netflix.eureka.http.handler.cluster;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.TokenClientProvider;
import com.alibaba.csp.sentinel.cluster.server.EmbeddedClusterTokenServerProvider;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.netflix.eureka.command.CommandResponse;

/**
 * set cluster mode, accept param: mode={0|1} 0:client mode 1:server mode
 * @author WX
 *
 */
@Endpoint(id = "setClusterMode")
public class ModifyClusterModeCommandHandler {

	@ReadOperation
    public CommandResponse<String> handle(int mode) {
        try {
            if (mode == ClusterStateManager.CLUSTER_CLIENT && !TokenClientProvider.isClientSpiAvailable()) {
                return CommandResponse.ofFailure("token client mode not available: no SPI found");
            }
            if (mode == ClusterStateManager.CLUSTER_SERVER && !isClusterServerSpiAvailable()) {
                return CommandResponse.ofFailure("token server mode not available: no SPI found");
            }
            RecordLog.info("[ModifyClusterModeCommandHandler] Modifying cluster mode to: {}", mode);

            ClusterStateManager.applyState(mode);
            return CommandResponse.ofSuccess("success");
        } catch (NumberFormatException ex) {
            return CommandResponse.ofFailure("invalid parameter");
        } catch (Exception ex) {
            return CommandResponse.ofFailure(ex.getMessage());
        }
    }

    private boolean isClusterServerSpiAvailable() {
        return EmbeddedClusterTokenServerProvider.isServerSpiAvailable();
    }
}
