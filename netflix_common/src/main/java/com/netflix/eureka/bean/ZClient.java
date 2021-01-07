package com.netflix.eureka.bean;

import com.alibaba.csp.sentinel.util.StringUtil;

public class ZClient {
	private String id;
	private String name;
	
	private String svrid;
	private String svrname;
	
	private String instanceId;
	
	private String zone;
	private String groupname;
	
	private Integer port;
	
	private String ipaddr;
	private String hostname;
	
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

	public String getZone() {
		return zone;
	}

	public String getGroupname() {
		return groupname;
	}

	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public String getIpaddr() {
		return ipaddr;
	}

	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}
	
	@Override
    public boolean equals(Object obj) {
		if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ZClient other = (ZClient) obj;
        if(StringUtil.isEmpty(other.getId()) || StringUtil.isEmpty(other.getHostname())) {
        	return false;
        }
        if(!(other.getId().equals(this.id) && other.getHostname().equals(this.hostname))) {
        	return false;
        }
        Integer svrPort = other.getPort();
        if(svrPort != null) {
        	if(!svrPort.equals(this.port)) {
        		return false;
        	}
        }
        String ipAddr = other.getIpaddr();
        if(ipAddr != null) {
        	if(!ipAddr.equals(this.ipaddr)) {
        		return false;
        	}
        }
        return true;
	}

	@Override
	public String toString() {
		StringBuffer json = new StringBuffer();
		json.append("{\"hostname\": \"").append(hostname == null ? "" : hostname);
		json.append("\", \"id\": \"").append(id == null ? "" : id);
		json.append("\", \"ipaddr\": \"").append(ipaddr == null ? "" : ipaddr);
		json.append("\", \"port\": \"").append(port == null ? "" : port);
		json.append("\"}");
		return json.toString();
	}
	
	
}
