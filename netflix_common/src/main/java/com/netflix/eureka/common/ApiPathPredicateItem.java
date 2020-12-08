package com.netflix.eureka.common;

import java.util.Objects;

import com.netflix.eureka.command.CommandConstants;

public class ApiPathPredicateItem {

    private String pattern;
    private int matchStrategy = CommandConstants.URL_MATCH_STRATEGY_EXACT;

    public ApiPathPredicateItem setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public ApiPathPredicateItem setMatchStrategy(int matchStrategy) {
        this.matchStrategy = matchStrategy;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public int getMatchStrategy() {
        return matchStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ApiPathPredicateItem that = (ApiPathPredicateItem)o;

        if (matchStrategy != that.matchStrategy) { return false; }
        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        int result = pattern != null ? pattern.hashCode() : 0;
        result = 31 * result + matchStrategy;
        return result;
    }

    @Override
    public String toString() {
        return "ApiPathPredicateItem{" +
            "pattern='" + pattern + '\'' +
            ", matchStrategy=" + matchStrategy +
            '}';
    }
}
