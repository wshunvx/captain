package com.netflix.eureka.found.model;

import java.util.ArrayList;
import java.util.List;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.provider.Serializer;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@Serializer
public class Cluster {
	private String id;
	private String name;
	
	private String svrid;
	
	private String instanceId;
	
	private Integer port;
	private Integer locked;
	private Integer status;
	
	private String ts;
	private String secret;

	
	private List<InstanceInfo> service;
	
	public static final class Builder {

    	@XStreamOmitField
        private Cluster result;

        private Builder() {
            result = new Cluster();
        }
        
        public Builder inId(String id) {
        	result.id = id;
        	return this;
        }
        
        public Builder inName(String name) {
        	result.name = name;
        	return this;
        }
        
        public Builder inSvrid(String svrid) {
        	result.svrid = svrid;
        	return this;
        }
        
        public Builder inPort(Integer port) {
        	result.port = port;
        	return this;
        }
        
        public Builder inSecret(String secret) {
        	result.secret = secret;
        	return this;
        }
        
        public Builder inInstanceId(String instanceId) {
        	result.instanceId = instanceId;
        	return this;
        }
        
        public Builder addService(InstanceInfo service) {
            if (result.service == null) {
                result.service = new ArrayList<InstanceInfo>();
            }
            result.service.add(service);
            return this;
        }
        
        public static Builder newBuilder() {
        	return new Builder();
        }
        
        public Cluster build() {
        	return result;
        }
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSvrid() {
		return svrid;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public Integer getPort() {
		return port;
	}

	public Integer getLocked() {
		return locked;
	}

	public Integer getStatus() {
		return status;
	}

	public String getTs() {
		return ts;
	}

	public String getSecret() {
		return secret;
	}

	public List<InstanceInfo> getService() {
		return service;
	}

}
