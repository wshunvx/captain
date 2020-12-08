package com.netflix.eureka.common;

import com.netflix.eureka.command.CommandConstants;

public class GatewayParamFlowItem {

    /**
     * Should be set when applying to parameter flow rules.
     */
    private Integer index;

    /**
     * Strategy for parsing item (e.g. client IP, arbitrary headers and URL parameters).
     */
    private int parseStrategy;
    /**
     * Field to get (only required for arbitrary headers or URL parameters mode).
     */
    private String fieldName;
    /**
     * Matching pattern. If not set, all values will be kept in LRU map.
     */
    private String pattern;
    /**
     * Matching strategy for item value.
     */
    private int matchStrategy = CommandConstants.PARAM_MATCH_STRATEGY_EXACT;

    public Integer getIndex() {
        return index;
    }

    public GatewayParamFlowItem setIndex(Integer index) {
        this.index = index;
        return this;
    }

    public int getParseStrategy() {
        return parseStrategy;
    }

    public GatewayParamFlowItem setParseStrategy(int parseStrategy) {
        this.parseStrategy = parseStrategy;
        return this;
    }

    public String getFieldName() {
        return fieldName;
    }

    public GatewayParamFlowItem setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public GatewayParamFlowItem setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public int getMatchStrategy() {
        return matchStrategy;
    }

    public GatewayParamFlowItem setMatchStrategy(int matchStrategy) {
        this.matchStrategy = matchStrategy;
        return this;
    }

    @Override
    public String toString() {
        return "GatewayParamFlowItem{" +
            "index=" + index +
            ", parseStrategy=" + parseStrategy +
            ", fieldName='" + fieldName + '\'' +
            ", pattern='" + pattern + '\'' +
            ", matchStrategy=" + matchStrategy +
            '}';
    }
}
