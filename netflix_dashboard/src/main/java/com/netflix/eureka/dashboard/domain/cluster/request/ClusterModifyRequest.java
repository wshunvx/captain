package com.netflix.eureka.dashboard.domain.cluster.request;

public interface ClusterModifyRequest {

    String getApp();

    String getId();

    Integer getMode();
}
