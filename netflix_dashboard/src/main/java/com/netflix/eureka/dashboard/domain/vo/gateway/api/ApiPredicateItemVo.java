package com.netflix.eureka.dashboard.domain.vo.gateway.api;

public class ApiPredicateItemVo {

    private String pattern;

    private Integer matchStrategy;

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
