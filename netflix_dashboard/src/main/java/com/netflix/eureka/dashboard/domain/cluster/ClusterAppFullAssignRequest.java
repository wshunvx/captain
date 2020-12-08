package com.netflix.eureka.dashboard.domain.cluster;

import java.util.List;
import java.util.Set;

import com.netflix.eureka.dashboard.domain.cluster.request.ClusterAppAssignMap;

public class ClusterAppFullAssignRequest {

    private List<ClusterAppAssignMap> clusterMap;
    private Set<String> remainingList;

    public List<ClusterAppAssignMap> getClusterMap() {
        return clusterMap;
    }

    public ClusterAppFullAssignRequest setClusterMap(
        List<ClusterAppAssignMap> clusterMap) {
        this.clusterMap = clusterMap;
        return this;
    }

    public Set<String> getRemainingList() {
        return remainingList;
    }

    public ClusterAppFullAssignRequest setRemainingList(Set<String> remainingList) {
        this.remainingList = remainingList;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterAppFullAssignRequest{" +
            "clusterMap=" + clusterMap +
            ", remainingList=" + remainingList +
            '}';
    }
}
