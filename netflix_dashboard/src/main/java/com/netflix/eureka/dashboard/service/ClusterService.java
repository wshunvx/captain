package com.netflix.eureka.dashboard.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.netflix.eureka.dashboard.domain.cluster.ClusterAppAssignResultVO;
import com.netflix.eureka.dashboard.domain.cluster.ClusterGroupEntity;
import com.netflix.eureka.dashboard.domain.cluster.request.ClusterAppAssignMap;
import com.netflix.eureka.dashboard.domain.cluster.request.ClusterClientModifyRequest;
import com.netflix.eureka.dashboard.domain.cluster.request.ClusterServerModifyRequest;
import com.netflix.eureka.dashboard.domain.cluster.state.ClusterUniversalStatePairVO;
import com.netflix.eureka.dashboard.domain.cluster.state.ClusterUniversalStateVO;

public interface ClusterService {

    /**
     * Unbind a specific cluster server and its clients.
     *
     * @param app app name
     * @param machineId valid machine ID ({@code host@commandPort})
     * @return assign result
     */
    ClusterAppAssignResultVO unbindClusterServer(String app, String machineId);

    /**
     * Unbind a set of cluster servers and its clients.
     *
     * @param app app name
     * @param machineIdSet set of valid machine ID ({@code host@commandPort})
     * @return assign result
     */
    ClusterAppAssignResultVO unbindClusterServers(String app, Set<String> machineIdSet);

    /**
     * Apply cluster server and client assignment for provided app.
     *
     * @param app app name
     * @param clusterMap cluster assign map (server -> clients)
     * @param remainingSet unassigned set of machine ID
     * @return assign result
     */
    ClusterAppAssignResultVO applyAssignToApp(String app, List<ClusterAppAssignMap> clusterMap,
                                              Set<String> remainingSet);
    
    public CompletableFuture<Void> modifyClusterClientConfig(ClusterClientModifyRequest request);
    public CompletableFuture<Void> modifyClusterServerConfig(ClusterServerModifyRequest request);
    public CompletableFuture<List<ClusterUniversalStatePairVO>> getClusterUniversalState(String app);
    public CompletableFuture<ClusterGroupEntity> getClusterUniversalStateForAppMachine(String app, String machineId);
    public CompletableFuture<ClusterUniversalStateVO> getClusterUniversalState(String app, String homePage);
}
