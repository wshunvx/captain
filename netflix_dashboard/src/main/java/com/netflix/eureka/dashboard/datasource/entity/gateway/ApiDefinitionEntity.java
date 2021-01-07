package com.netflix.eureka.dashboard.datasource.entity.gateway;

import java.util.Date;

import com.alibaba.csp.sentinel.slots.block.Rule;
import com.netflix.eureka.dashboard.datasource.entity.rule.RuleEntity;

public class ApiDefinitionEntity extends RuleEntity {

    private String serviceId;
    
    private String app;
    private String instanceId;

    private Date gmtCreate;
    private Date gmtModified;

    private String apiName;
    
    private String url;
    private String pattern;

    private Integer matchStrategy;
    private Integer stripPrefix;
    
    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

    public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Integer getMatchStrategy() {
		return matchStrategy;
	}

	public void setMatchStrategy(Integer matchStrategy) {
		this.matchStrategy = matchStrategy;
	}

	public Integer getStripPrefix() {
		return stripPrefix;
	}

	public void setStripPrefix(Integer stripPrefix) {
		this.stripPrefix = stripPrefix;
	}

	@Override
    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    @Override
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

	@Override
    public Rule toRule() {
        return null;
    }

    @Override
    public String toString() {
        return "ApiDefinitionEntity{" +
                "id=" + getId() +
                ", serviceId='" + serviceId + '\'' +
                ", app='" + app + '\'' +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                ", apiName='" + apiName + '\'' +
                '}';
    }
}
