package com.netflix.eureka.dashboard.domain.cluster.config;

public class ServerTransportConfig {

    public static final int DEFAULT_PORT = 18730;
    public static final int DEFAULT_IDLE_SECONDS = 600;

    private Integer port;
    private Integer idleSeconds;

    public ServerTransportConfig() {
        this(DEFAULT_PORT, DEFAULT_IDLE_SECONDS);
    }

    public ServerTransportConfig(Integer port, Integer idleSeconds) {
        this.port = port;
        this.idleSeconds = idleSeconds;
    }

    public Integer getPort() {
        return port;
    }

    public ServerTransportConfig setPort(Integer port) {
        this.port = port;
        return this;
    }

    public Integer getIdleSeconds() {
        return idleSeconds;
    }

    public ServerTransportConfig setIdleSeconds(Integer idleSeconds) {
        this.idleSeconds = idleSeconds;
        return this;
    }

    @Override
    public String toString() {
        return "ServerTransportConfig{" +
            "port=" + port +
            ", idleSeconds=" + idleSeconds +
            '}';
    }
}
