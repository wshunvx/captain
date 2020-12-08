package com.netflix.eureka.dashboard.datasource.entity.gateway;

import java.util.Date;
import java.util.Objects;

import com.alibaba.csp.sentinel.slots.block.Rule;
import com.netflix.eureka.common.ApiDefinition;
import com.netflix.eureka.common.ApiPathPredicateItem;
import com.netflix.eureka.dashboard.datasource.entity.rule.RuleEntity;

public class ApiDefinitionEntity implements RuleEntity {

    private Long id;
    private String serviceId;
    
    private String app;
    private String instanceId;

    private Date gmtCreate;
    private Date gmtModified;

    private String apiName;
    private ApiPredicateItemEntity predicateItems;


    public ApiDefinition toApiDefinition() {
        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setApiName(apiName);
        apiDefinition.setServiceId(serviceId);

        if (predicateItems != null) {
        	ApiPathPredicateItem apiPredicateItem = new ApiPathPredicateItem();
            apiPredicateItem.setMatchStrategy(predicateItems.getMatchStrategy());
            apiPredicateItem.setPattern(predicateItems.getPattern());
            apiDefinition.setPredicateItems(apiPredicateItem);
        }

        return apiDefinition;
    }

    public ApiDefinitionEntity() {

    }

    public ApiDefinitionEntity(String apiName, ApiPredicateItemEntity predicateItems) {
        this.apiName = apiName;
        this.predicateItems = predicateItems;
    }

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

	public ApiPredicateItemEntity getPredicateItems() {
        return predicateItems;
    }

    public void setPredicateItems(ApiPredicateItemEntity predicateItems) {
        this.predicateItems = predicateItems;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
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

	@Override
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
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        ApiDefinitionEntity entity = (ApiDefinitionEntity) o;
        return Objects.equals(id, entity.id) &&
        		Objects.equals(serviceId, entity.serviceId) &&
                Objects.equals(app, entity.app) &&
                Objects.equals(gmtCreate, entity.gmtCreate) &&
                Objects.equals(gmtModified, entity.gmtModified) &&
                Objects.equals(apiName, entity.apiName) &&
                Objects.equals(predicateItems, entity.predicateItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serviceId, app, gmtCreate, gmtModified, apiName, predicateItems);
    }

    @Override
    public String toString() {
        return "ApiDefinitionEntity{" +
                "id=" + id +
                ", serviceId='" + serviceId + '\'' +
                ", app='" + app + '\'' +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                ", apiName='" + apiName + '\'' +
                ", predicateItems=" + predicateItems +
                '}';
    }
}
