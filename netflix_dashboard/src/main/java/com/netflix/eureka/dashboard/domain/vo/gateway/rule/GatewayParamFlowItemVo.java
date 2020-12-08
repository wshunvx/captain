package com.netflix.eureka.dashboard.domain.vo.gateway.rule;

public class GatewayParamFlowItemVo {

    private Integer parseStrategy;

    private String fieldName;

    private String pattern;

    private Integer matchStrategy;

    public Integer getParseStrategy() {
        return parseStrategy;
    }

    public void setParseStrategy(Integer parseStrategy) {
        this.parseStrategy = parseStrategy;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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
}
