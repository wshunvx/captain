package com.netflix.eureka.webflux.reactor;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

public class ContextConfig {

    private final String contextName;
    private final String origin;

    public ContextConfig(String contextName) {
        this(contextName, "");
    }

    public ContextConfig(String contextName, String origin) {
        AssertUtil.assertNotBlank(contextName, "contextName cannot be blank");
        this.contextName = contextName;
        if (StringUtil.isBlank(origin)) {
            origin = "";
        }
        this.origin = origin;
    }

    public String getContextName() {
        return contextName;
    }

    public String getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return "ContextConfig{" +
            "contextName='" + contextName + '\'' +
            ", origin='" + origin + '\'' +
            '}';
    }
}
