package com.netflix.eureka.http.auth;

import java.security.KeyPair;
import java.util.Collection;

import org.springframework.cloud.netflix.zuul.filters.Route;

import com.netflix.eureka.bean.ZUri;

public interface UriCache {
	void init(String password);
    
    KeyPair keyPair();
    
	void invalidate(String svrid, long registrationTimestamp, Collection<ZUri> uri);
	/**
	 * Verify rules according to path.
	 * @param svrid
	 * @param path
	 * @return
	 */
     boolean verification(String svrid, String method, Route route);

    /**
     * Performs a shutdown of this cache by stopping internal threads and unregistering
     * Servo monitors.
     */
    void stop();
    
    
}
