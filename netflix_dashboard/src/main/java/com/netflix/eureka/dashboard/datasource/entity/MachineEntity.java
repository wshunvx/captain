package com.netflix.eureka.dashboard.datasource.entity;

import java.util.Date;

public class MachineEntity {
	private String app;
    private String instanceId;
    
    private String ip;
    private String hostname;
    private Long timestamp;
    private Integer port;
    private String status;
    private Date gmtCreate;
    private Date gmtModified;

    
    public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
    public String toString() {
        return "MachineEntity{" +
            "id=" + instanceId +
            ", gmtCreate=" + gmtCreate +
            ", gmtModified=" + gmtModified +
            ", app='" + app + '\'' +
            ", ip='" + ip + '\'' +
            ", hostname='" + hostname + '\'' +
            ", timestamp=" + timestamp +
            ", port=" + port +
            '}';
    }
}
