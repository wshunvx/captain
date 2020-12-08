package com.netflix.eureka.dashboard.domain.cluster.state;

public class ClusterUniversalStateVO {

    private ClusterStateSimpleEntity stateInfo;
    private ClusterClientStateVO client;
    private ClusterServerStateVO server;

    public ClusterClientStateVO getClient() {
        return client;
    }

    public ClusterUniversalStateVO setClient(ClusterClientStateVO client) {
        this.client = client;
        return this;
    }

    public ClusterServerStateVO getServer() {
        return server;
    }

    public ClusterUniversalStateVO setServer(ClusterServerStateVO server) {
        this.server = server;
        return this;
    }

    public ClusterStateSimpleEntity getStateInfo() {
        return stateInfo;
    }

    public ClusterUniversalStateVO setStateInfo(
        ClusterStateSimpleEntity stateInfo) {
        this.stateInfo = stateInfo;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterUniversalStateVO{" +
            "stateInfo=" + stateInfo +
            ", client=" + client +
            ", server=" + server +
            '}';
    }
}
