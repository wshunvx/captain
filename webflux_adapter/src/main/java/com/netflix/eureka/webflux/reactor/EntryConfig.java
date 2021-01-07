package com.netflix.eureka.webflux.reactor;

import java.util.Arrays;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.util.AssertUtil;

public class EntryConfig {

    private final String resourceName;
    private final EntryType entryType;
    private final int resourceType;

    private final int acquireCount;
    private final Object[] args;
    private final ContextConfig contextConfig;

    public EntryConfig(String resourceName) {
        this(resourceName, EntryType.OUT);
    }

    public EntryConfig(String resourceName, EntryType entryType) {
        this(resourceName, entryType, null);
    }

    public EntryConfig(String resourceName, EntryType entryType, ContextConfig contextConfig) {
        this(resourceName, entryType, 1, new Object[0], contextConfig);
    }

    public EntryConfig(String resourceName, int resourceType, EntryType entryType, ContextConfig contextConfig) {
        this(resourceName, resourceType, entryType, 1, new Object[0], contextConfig);
    }

    public EntryConfig(String resourceName, EntryType entryType, int acquireCount, Object[] args) {
        this(resourceName, entryType, acquireCount, args, null);
    }

    public EntryConfig(String resourceName, EntryType entryType, int acquireCount, Object[] args,
                       ContextConfig contextConfig) {
        this(resourceName, ResourceTypeConstants.COMMON, entryType, acquireCount, args, contextConfig);
    }

    public EntryConfig(String resourceName, int resourceType, EntryType entryType, int acquireCount, Object[] args) {
        this(resourceName, resourceType, entryType, acquireCount, args, null);
    }

    public EntryConfig(String resourceName, int resourceType, EntryType entryType, int acquireCount, Object[] args,
                       ContextConfig contextConfig) {
        AssertUtil.assertNotBlank(resourceName, "resourceName cannot be blank");
        AssertUtil.notNull(entryType, "entryType cannot be null");
        AssertUtil.isTrue(acquireCount > 0, "acquireCount should be positive");
        this.resourceName = resourceName;
        this.entryType = entryType;
        this.resourceType = resourceType;
        this.acquireCount = acquireCount;
        this.args = args;
        // Constructed ContextConfig should be valid here. Null is allowed here.
        this.contextConfig = contextConfig;
    }

    public String getResourceName() {
        return resourceName;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public int getAcquireCount() {
        return acquireCount;
    }

    public Object[] getArgs() {
        return args;
    }

    public ContextConfig getContextConfig() {
        return contextConfig;
    }

    public int getResourceType() {
        return resourceType;
    }

    @Override
    public String toString() {
        return "EntryConfig{" +
            "resourceName='" + resourceName + '\'' +
            ", entryType=" + entryType +
            ", resourceType=" + resourceType +
            ", acquireCount=" + acquireCount +
            ", args=" + Arrays.toString(args) +
            ", contextConfig=" + contextConfig +
            '}';
    }
}
