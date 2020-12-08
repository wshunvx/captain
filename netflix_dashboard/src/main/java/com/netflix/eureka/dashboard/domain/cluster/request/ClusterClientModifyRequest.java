package com.netflix.eureka.dashboard.domain.cluster.request;

import com.netflix.eureka.dashboard.domain.cluster.config.ClusterClientConfig;

public class ClusterClientModifyRequest implements ClusterModifyRequest {

    private String app;
    private String id;

    private Integer mode;
    private ClusterClientConfig clientConfig;

    @Override
    public String getApp() {
        return app;
    }

    public ClusterClientModifyRequest setApp(String app) {
        this.app = app;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    public ClusterClientModifyRequest setId(String id) {
        this.id = id;
        return this;
    }


    @Override
    public Integer getMode() {
        return mode;
    }

    public ClusterClientModifyRequest setMode(Integer mode) {
        this.mode = mode;
        return this;
    }

    public ClusterClientConfig getClientConfig() {
        return clientConfig;
    }

    public ClusterClientModifyRequest setClientConfig(
        ClusterClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }
}
