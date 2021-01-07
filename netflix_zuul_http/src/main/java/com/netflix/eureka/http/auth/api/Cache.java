package com.netflix.eureka.http.auth.api;

public class Cache<T> {
	enum Action {
        Register, Cancel, Renew
    };

    public static final int DEFAULT_DURATION_IN_SECS = 90;

    private T uri;
    private long registrationTimestamp;
    // Make it volatile so that the expiration task would see this quicker
    private volatile long lastUpdateTimestamp;

    public Cache(T r, long regTimestamp) {
    	uri = r;
        lastUpdateTimestamp = System.currentTimeMillis();
        registrationTimestamp = regTimestamp;
    }
    
    public void replace(T r, long regTimestamp) {
    	uri = r;
    	registrationTimestamp = regTimestamp;
    }

    /**
     * Renew the lease, use renewal duration if it was specified by the
     * associated {@link T} during registration, otherwise default duration is
     * {@link #DEFAULT_DURATION_IN_SECS}.
     */
    public void renew() {
        lastUpdateTimestamp = System.currentTimeMillis();

    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public long getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public T getUri() {
        return uri;
    }
}
