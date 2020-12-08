package com.netflix.eureka.http.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.alibaba.csp.sentinel.log.RecordLog;

public final class GatewayRegexCache {

    private static final Map<String, Pattern> REGEX_CACHE = new ConcurrentHashMap<>();

    public static Pattern getRegexPattern(String pattern) {
        if (pattern == null) {
            return null;
        }
        return REGEX_CACHE.get(pattern);
    }

    public static boolean addRegexPattern(String pattern) {
        if (pattern == null) {
            return false;
        }
        try {
            Pattern regex = Pattern.compile(pattern);
            REGEX_CACHE.put(pattern, regex);
            return true;
        } catch (Exception ex) {
            RecordLog.warn("[GatewayRegexCache] Failed to compile the regex: " + pattern, ex);
            return false;
        }
    }

    public static void clear() {
        REGEX_CACHE.clear();
    }

    private GatewayRegexCache() {}
}
