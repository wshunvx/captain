package com.netflix.eureka.found.sentinel;

public class ServerContextHolder {
	private final ServerContext serverContext;

    private ServerContextHolder(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    public ServerContext getServerContext() {
        return this.serverContext;
    }

    private static ServerContextHolder holder;

    public static synchronized void initialize(ServerContext serverContext) {
        holder = new ServerContextHolder(serverContext);
    }

    public static ServerContextHolder getSecurity() {
        return holder;
    }
}
