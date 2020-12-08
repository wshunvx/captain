package com.netflix.eureka.found.model;

import com.netflix.discovery.provider.Serializer;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@Serializer
public class Configinfo {
	
	private String dataId;
    private String appName;
    private String group;
    private String content;
    
	public static final class Builder {

    	@XStreamOmitField
        private Configinfo result;

        private Builder() {
            result = new Configinfo();
        }
        
        public static Builder newBuilder() {
        	return new Builder();
        }

        public Builder withBase(String id, String appName) {
            result.dataId = id;
            result.appName = appName;
            return this;
        }
        
        public Builder inGroup(String group) {
            result.group = group;
            return this;
        }
        
        public Builder inContent(String content) {
            result.content = content;
            return this;
        }

        public Configinfo build() {
        	return result;
        }
    }
	
	public String getDataId() {
		return dataId;
	}
	public String getAppName() {
		return appName;
	}
	public String getGroup() {
		return group;
	}
	public String getContent() {
		return content;
	}
    
}
