package com.netflix.eureka.bean;

public class ZClient {
	private String id;
	private String name;
	
	private String svrid;
	private String svrname;
	
	private String instanceId;
	
	private String path;
	private Integer port;
	
	private String ts;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSvrid() {
		return svrid;
	}

	public void setSvrid(String svrid) {
		this.svrid = svrid;
	}

	public String getSvrname() {
		return svrname;
	}

	public void setSvrname(String svrname) {
		this.svrname = svrname;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}
	
}
