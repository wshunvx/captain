package com.netflix.eureka.dashboard.domain.cluster.state;

public class ClusterRequestLimitVO {

    private String namespace;
    private Double currentQps;
    private Double maxAllowedQps;

    public String getNamespace() {
        return namespace;
    }

    public ClusterRequestLimitVO setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public Double getCurrentQps() {
        return currentQps;
    }

    public ClusterRequestLimitVO setCurrentQps(Double currentQps) {
        this.currentQps = currentQps;
        return this;
    }

    public Double getMaxAllowedQps() {
        return maxAllowedQps;
    }

    public ClusterRequestLimitVO setMaxAllowedQps(Double maxAllowedQps) {
        this.maxAllowedQps = maxAllowedQps;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterRequestLimitVO{" +
            "namespace='" + namespace + '\'' +
            ", currentQps=" + currentQps +
            ", maxAllowedQps=" + maxAllowedQps +
            '}';
    }
}
