package com.netflix.eureka.found.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.discovery.provider.Serializer;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@Serializer
public class Namespace {
	
	private String id;
	private String zone;

    private String hostname;
    private String groupname;
    private String ipaddr;
    private Integer status;
    @XStreamOmitField
    private volatile List<Cluster> cluster;
    @XStreamOmitField
    private volatile Serviceinfo serviceinfo;
    
    
    public static final class Builder {

    	@XStreamOmitField
        private Namespace result;
        
        private Builder() {
            result = new Namespace();
        }
        
        public static Builder newBuilder() {
        	return new Builder();
        }

        public Builder withBase(String id, String zone) {
            result.id = id;
            result.zone = zone;
            return this;
        }
        
        public Builder inId(String id) {
        	result.id = id;
        	return this;
        }
        
        public Builder inZone(String zone) {
        	result.zone = zone;
        	return this;
        }
        
        public Builder inHostname(String hostname) {
        	result.hostname = hostname;
        	return this;
        }
        
        public Builder inGroup(String group) {
        	result.groupname = group;
        	return this;
        }
        
        public Builder inIpaddr(String ipaddr) {
        	result.ipaddr = ipaddr;
        	return this;
        }
        
        public Builder inStatus(Integer status) {
        	result.status = status;
        	return this;
        }

        public Builder setServiceinfo(Serviceinfo serviceinfo) {
            result.serviceinfo = serviceinfo;
            return this;
        }
        
        public Namespace build() {
        	return result;
        }
    }
    
    @JsonProperty("cluster")
    public List<Cluster> getCluster() {
        return cluster;
    }
    
    @JsonProperty("service")
    public Serviceinfo getServiceinfo() {
		return serviceinfo;
	}

    public String getId() {
		return id;
	}

	public String getZone() {
		return zone;
	}

	public String getHostname() {
		return hostname;
	}

	public String getGroupname() {
		return groupname;
	}

	public String getIpaddr() {
		return ipaddr;
	}

	public Integer getStatus() {
		return status;
	}

}
