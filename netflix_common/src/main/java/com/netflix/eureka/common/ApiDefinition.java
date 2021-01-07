package com.netflix.eureka.common;

import java.util.Objects;

import com.netflix.eureka.command.CommandConstants;

public class ApiDefinition {

	private String serviceId;
    private String apiName;
    
    private String url;
    private String pattern;
    private int stripPrefix = CommandConstants.STRIP_PREFIX_ROUTE_TRUE;
    private int matchStrategy = CommandConstants.URL_MATCH_STRATEGY_EXACT;
    
    private int status = 0; // 0.Insert 1.Update 2.Remove
    
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

	public int getStripPrefix() {
		return stripPrefix;
	}

	public void setStripPrefix(int stripPrefix) {
		this.stripPrefix = stripPrefix;
	}

	public int getMatchStrategy() {
		return matchStrategy;
	}

	public void setMatchStrategy(int matchStrategy) {
		this.matchStrategy = matchStrategy;
	}

	public int getStatus() {
		return status;
	}

	public String getApiName() {
        return apiName;
    }

    public ApiDefinition setApiName(String apiName) {
        this.apiName = apiName;
        return this;
    }

    public ApiDefinition setStatus(int status) {
		this.status = status;
		return this;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ApiDefinition that = (ApiDefinition) o;

        return Objects.equals(apiName, that.apiName) && Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiName, serviceId);
    }

    @Override
    public String toString() {
        return "ApiDefinition{" +
            "apiName='" + apiName + '\'' +
            ", serviceId='" + serviceId + '\'' +
            '}';
    }
}
