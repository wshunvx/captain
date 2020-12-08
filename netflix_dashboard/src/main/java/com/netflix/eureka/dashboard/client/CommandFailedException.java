package com.netflix.eureka.dashboard.client;

public class CommandFailedException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CommandFailedException() {}

    public CommandFailedException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
