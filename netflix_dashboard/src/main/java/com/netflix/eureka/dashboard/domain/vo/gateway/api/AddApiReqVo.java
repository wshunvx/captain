package com.netflix.eureka.dashboard.domain.vo.gateway.api;

public class AddApiReqVo {
    private Long id;
    private String serviceId;
    
    private String apiName;

    private ApiPredicateItemVo predicateItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public ApiPredicateItemVo getPredicateItems() {
        return predicateItems;
    }

    public void setPredicateItems(ApiPredicateItemVo predicateItems) {
        this.predicateItems = predicateItems;
    }
}

