package com.netflix.eureka.found.sentinel;

public class SentinelServerContextHolder {
	private final SentinelServerContext serverContext;

    private SentinelServerContextHolder(SentinelServerContext serverContext) {
        this.serverContext = serverContext;
    }

    public SentinelServerContext getServerContext() {
        return this.serverContext;
    }

    private static SentinelServerContextHolder holder;

    public static synchronized void initialize(SentinelServerContext serverContext) {
        holder = new SentinelServerContextHolder(serverContext);
    }

    public static SentinelServerContextHolder getSentinel() {
        return holder;
    }
}
