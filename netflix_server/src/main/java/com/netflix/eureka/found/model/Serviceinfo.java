package com.netflix.eureka.found.model;

import com.netflix.discovery.provider.Serializer;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@Serializer
public class Serviceinfo {
	private String name;
    private int groupCount = 0;
    private int clusterCount = 0;
    private int ipCount = 0;
    private int healthyInstanceCount = 0;
    
    public static final class Builder {

    	@XStreamOmitField
        private Serviceinfo result;

        private Builder() {
            result = new Serviceinfo();
        }
        
        public static Builder newBuilder() {
        	return new Builder();
        }

        public Builder withBase(String name) {
            result.name = name;
            return this;
        }
        
        public Builder inGroupCount(int groupCount) {
            result.groupCount = groupCount;
            return this;
        }
        
        public Builder inClusterCount(int clusterCount) {
            result.clusterCount = clusterCount;
            return this;
        }
        
        public Builder inIpCount(int ipCount) {
            result.ipCount = ipCount;
            return this;
        }
        
        public Builder inHealthyInstanceCount(int healthyInstanceCount) {
            result.healthyInstanceCount = healthyInstanceCount;
            return this;
        }

        public Serviceinfo build() {
        	return result;
        }
    }

	public String getName() {
		return name;
	}

	public int getGroupCount() {
		return groupCount;
	}

	public int getClusterCount() {
		return clusterCount;
	}

	public int getIpCount() {
		return ipCount;
	}

	public int getHealthyInstanceCount() {
		return healthyInstanceCount;
	}

}
