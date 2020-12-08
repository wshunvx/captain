package com.netflix.eureka.common;

import java.util.Objects;

public class ApiDefinition {

	private String serviceId;
    private String apiName;
    
    private ApiPathPredicateItem predicateItems;

    public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

    public String getApiName() {
        return apiName;
    }

    public ApiDefinition setApiName(String apiName) {
        this.apiName = apiName;
        return this;
    }

    public ApiPathPredicateItem getPredicateItems() {
        return predicateItems;
    }

    public ApiDefinition setPredicateItems(ApiPathPredicateItem predicateItems) {
        this.predicateItems = predicateItems;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ApiDefinition that = (ApiDefinition) o;

        if (!Objects.equals(apiName, that.apiName)) { return false; }
        if (!Objects.equals(serviceId, that.serviceId)) { return false; }
        return Objects.equals(predicateItems, that.predicateItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiName, serviceId, predicateItems);
    }

    @Override
    public String toString() {
        return "ApiDefinition{" +
            "apiName='" + apiName + '\'' +
            ", serviceId='" + serviceId + '\'' +
            ", predicateItems=" + predicateItems +
            '}';
    }
}
