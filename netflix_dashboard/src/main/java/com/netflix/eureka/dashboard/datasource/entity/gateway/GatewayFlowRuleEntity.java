package com.netflix.eureka.dashboard.datasource.entity.gateway;

import java.util.Date;
import java.util.Objects;

import com.alibaba.csp.sentinel.slots.block.Rule;
import com.netflix.eureka.common.GatewayFlowRule;
import com.netflix.eureka.common.GatewayParamFlowItem;
import com.netflix.eureka.dashboard.datasource.entity.rule.RuleEntity;

public class GatewayFlowRuleEntity implements RuleEntity {

    /**闂撮殧鍗曚綅*/
    /**0-绉�*/
    public static final int INTERVAL_UNIT_SECOND = 0;
    /**1-鍒�*/
    public static final int INTERVAL_UNIT_MINUTE = 1;
    /**2-鏃�*/
    public static final int INTERVAL_UNIT_HOUR = 2;
    /**3-澶�*/
    public static final int INTERVAL_UNIT_DAY = 3;

    private Long id;
    private String app;
    private String instanceId;

    private Date gmtCreate;
    private Date gmtModified;

    private String resource;
    private Integer resourceMode;

    private Integer grade;
    private Double count;
    private Long interval;
    private Integer intervalUnit;

    private Integer controlBehavior;
    private Integer burst;

    private Integer maxQueueingTimeoutMs;

    private GatewayParamFlowItemEntity paramItem;

    public static Long calIntervalSec(Long interval, Integer intervalUnit) {
        switch (intervalUnit) {
            case INTERVAL_UNIT_SECOND:
                return interval;
            case INTERVAL_UNIT_MINUTE:
                return interval * 60;
            case INTERVAL_UNIT_HOUR:
                return interval * 60 * 60;
            case INTERVAL_UNIT_DAY:
                return interval * 60 * 60 * 24;
            default:
                break;
        }

        throw new IllegalArgumentException("Invalid intervalUnit: " + intervalUnit);
    }

    public static Object[] parseIntervalSec(Long intervalSec) {
        if (intervalSec % (60 * 60 * 24) == 0) {
            return new Object[] {intervalSec / (60 * 60 * 24), INTERVAL_UNIT_DAY};
        }

        if (intervalSec % (60 * 60 ) == 0) {
            return new Object[] {intervalSec / (60 * 60), INTERVAL_UNIT_HOUR};
        }

        if (intervalSec % 60 == 0) {
            return new Object[] {intervalSec / 60, INTERVAL_UNIT_MINUTE};
        }

        return new Object[] {intervalSec, INTERVAL_UNIT_SECOND};
    }

    public GatewayFlowRule toGatewayFlowRule() {
        GatewayFlowRule rule = new GatewayFlowRule();
        rule.setResource(resource);
        rule.setResourceMode(resourceMode);

        rule.setGrade(grade);
        rule.setCount(count);
        rule.setIntervalSec(calIntervalSec(interval, intervalUnit));

        rule.setControlBehavior(controlBehavior);

        if (burst != null) {
            rule.setBurst(burst);
        }

        if (maxQueueingTimeoutMs != null) {
            rule.setMaxQueueingTimeoutMs(maxQueueingTimeoutMs);
        }

        if (paramItem != null) {
            GatewayParamFlowItem ruleItem = new GatewayParamFlowItem();
            rule.setParamItem(ruleItem);
            ruleItem.setParseStrategy(paramItem.getParseStrategy());
            ruleItem.setFieldName(paramItem.getFieldName());
            ruleItem.setPattern(paramItem.getPattern());

            if (paramItem.getMatchStrategy() != null) {
                ruleItem.setMatchStrategy(paramItem.getMatchStrategy());
            }
        }

        return rule;
    }

    public static GatewayFlowRuleEntity fromGatewayFlowRule(String app, GatewayFlowRule rule) {
        GatewayFlowRuleEntity entity = new GatewayFlowRuleEntity();
        entity.setApp(app);

        entity.setResource(rule.getResource());
        entity.setResourceMode(rule.getResourceMode());

        entity.setGrade(rule.getGrade());
        entity.setCount(rule.getCount());
        Object[] intervalSecResult = parseIntervalSec(rule.getIntervalSec());
        entity.setInterval((Long) intervalSecResult[0]);
        entity.setIntervalUnit((Integer) intervalSecResult[1]);

        entity.setControlBehavior(rule.getControlBehavior());
        entity.setBurst(rule.getBurst());
        entity.setMaxQueueingTimeoutMs(rule.getMaxQueueingTimeoutMs());

        GatewayParamFlowItem paramItem = rule.getParamItem();
        if (paramItem != null) {
            GatewayParamFlowItemEntity itemEntity = new GatewayParamFlowItemEntity();
            entity.setParamItem(itemEntity);
            itemEntity.setParseStrategy(paramItem.getParseStrategy());
            itemEntity.setFieldName(paramItem.getFieldName());
            itemEntity.setPattern(paramItem.getPattern());
            itemEntity.setMatchStrategy(paramItem.getMatchStrategy());
        }

        return entity;
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

    @Override
    public Rule toRule() {
        return null;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public GatewayParamFlowItemEntity getParamItem() {
        return paramItem;
    }

    public void setParamItem(GatewayParamFlowItemEntity paramItem) {
        this.paramItem = paramItem;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Integer getResourceMode() {
        return resourceMode;
    }

    public void setResourceMode(Integer resourceMode) {
        this.resourceMode = resourceMode;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    public Integer getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(Integer intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public Integer getControlBehavior() {
        return controlBehavior;
    }

    public void setControlBehavior(Integer controlBehavior) {
        this.controlBehavior = controlBehavior;
    }

    public Integer getBurst() {
        return burst;
    }

    public void setBurst(Integer burst) {
        this.burst = burst;
    }

    public Integer getMaxQueueingTimeoutMs() {
        return maxQueueingTimeoutMs;
    }

    public void setMaxQueueingTimeoutMs(Integer maxQueueingTimeoutMs) {
        this.maxQueueingTimeoutMs = maxQueueingTimeoutMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        GatewayFlowRuleEntity that = (GatewayFlowRuleEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(app, that.app) &&
                Objects.equals(gmtCreate, that.gmtCreate) &&
                Objects.equals(gmtModified, that.gmtModified) &&
                Objects.equals(resource, that.resource) &&
                Objects.equals(resourceMode, that.resourceMode) &&
                Objects.equals(grade, that.grade) &&
                Objects.equals(count, that.count) &&
                Objects.equals(interval, that.interval) &&
                Objects.equals(intervalUnit, that.intervalUnit) &&
                Objects.equals(controlBehavior, that.controlBehavior) &&
                Objects.equals(burst, that.burst) &&
                Objects.equals(maxQueueingTimeoutMs, that.maxQueueingTimeoutMs) &&
                Objects.equals(paramItem, that.paramItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, app, gmtCreate, gmtModified, resource, resourceMode, grade, count, interval, intervalUnit, controlBehavior, burst, maxQueueingTimeoutMs, paramItem);
    }

    @Override
    public String toString() {
        return "GatewayFlowRuleEntity{" +
                "id=" + id +
                ", app='" + app + '\'' +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                ", resource='" + resource + '\'' +
                ", resourceMode=" + resourceMode +
                ", grade=" + grade +
                ", count=" + count +
                ", interval=" + interval +
                ", intervalUnit=" + intervalUnit +
                ", controlBehavior=" + controlBehavior +
                ", burst=" + burst +
                ", maxQueueingTimeoutMs=" + maxQueueingTimeoutMs +
                ", paramItem=" + paramItem +
                '}';
    }
}
