package com.netflix.eureka.dashboard.client;

public class CommandNotFoundException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CommandNotFoundException() { }

    public CommandNotFoundException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
