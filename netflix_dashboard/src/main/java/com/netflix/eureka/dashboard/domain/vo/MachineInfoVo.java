package com.netflix.eureka.dashboard.domain.vo;

public class MachineInfoVo {

    private String app;
    private String hostname;
    private String ip;
    private int port;
    private long heartbeatVersion;
    private long lastHeartbeat;
    private boolean healthy;

    private String version;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    
    public void setHeartbeatVersion(long heartbeatVersion) {
        this.heartbeatVersion = heartbeatVersion;
    }
    
    public long getHeartbeatVersion() {
        return heartbeatVersion;
    }

    public String getVersion() {
        return version;
    }

    public MachineInfoVo setVersion(String version) {
        this.version = version;
        return this;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
}
