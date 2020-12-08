package com.netflix.eureka.dashboard.domain.cluster;

import java.util.Set;

import com.netflix.eureka.dashboard.domain.cluster.request.ClusterAppAssignMap;

public class ClusterAppSingleServerAssignRequest {

    private ClusterAppAssignMap clusterMap;
    private Set<String> remainingList;

    public ClusterAppAssignMap getClusterMap() {
        return clusterMap;
    }

    public ClusterAppSingleServerAssignRequest setClusterMap(ClusterAppAssignMap clusterMap) {
        this.clusterMap = clusterMap;
        return this;
    }

    public Set<String> getRemainingList() {
        return remainingList;
    }

    public ClusterAppSingleServerAssignRequest setRemainingList(Set<String> remainingList) {
        this.remainingList = remainingList;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterAppSingleServerAssignRequest{" +
            "clusterMap=" + clusterMap +
            ", remainingList=" + remainingList +
            '}';
    }
}
