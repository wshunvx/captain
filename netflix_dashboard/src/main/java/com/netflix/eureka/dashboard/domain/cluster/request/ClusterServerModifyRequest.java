package com.netflix.eureka.dashboard.domain.cluster.request;

import java.util.Set;

import com.netflix.eureka.dashboard.domain.cluster.config.ServerFlowConfig;
import com.netflix.eureka.dashboard.domain.cluster.config.ServerTransportConfig;

public class ClusterServerModifyRequest implements ClusterModifyRequest {

    private String app;
    private String id;

    private Integer mode;
    private ServerFlowConfig flowConfig;
    private ServerTransportConfig transportConfig;
    private Set<String> namespaceSet;

    @Override
    public String getApp() {
        return app;
    }

    public ClusterServerModifyRequest setApp(String app) {
        this.app = app;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    public ClusterServerModifyRequest setIp(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Integer getMode() {
        return mode;
    }

    public ClusterServerModifyRequest setMode(Integer mode) {
        this.mode = mode;
        return this;
    }

    public ServerFlowConfig getFlowConfig() {
        return flowConfig;
    }

    public ClusterServerModifyRequest setFlowConfig(
        ServerFlowConfig flowConfig) {
        this.flowConfig = flowConfig;
        return this;
    }

    public ServerTransportConfig getTransportConfig() {
        return transportConfig;
    }

    public ClusterServerModifyRequest setTransportConfig(
        ServerTransportConfig transportConfig) {
        this.transportConfig = transportConfig;
        return this;
    }

    public Set<String> getNamespaceSet() {
        return namespaceSet;
    }

    public ClusterServerModifyRequest setNamespaceSet(Set<String> namespaceSet) {
        this.namespaceSet = namespaceSet;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterServerModifyRequest{" +
            "app='" + app + '\'' +
            ", id='" + id + '\'' +
            ", mode=" + mode +
            ", flowConfig=" + flowConfig +
            ", transportConfig=" + transportConfig +
            ", namespaceSet=" + namespaceSet +
            '}';
    }
}
