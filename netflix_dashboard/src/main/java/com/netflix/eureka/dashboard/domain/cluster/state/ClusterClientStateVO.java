package com.netflix.eureka.dashboard.domain.cluster.state;

import com.netflix.eureka.dashboard.domain.cluster.ClusterClientInfoVO;

public class ClusterClientStateVO {

    /**
     * Cluster token client state.
     */
    private ClusterClientInfoVO clientConfig;

    public ClusterClientInfoVO getClientConfig() {
        return clientConfig;
    }

    public ClusterClientStateVO setClientConfig(ClusterClientInfoVO clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterClientStateVO{" +
            "clientConfig=" + clientConfig +
            '}';
    }
}
