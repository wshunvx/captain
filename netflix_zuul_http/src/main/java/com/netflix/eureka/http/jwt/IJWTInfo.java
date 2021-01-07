package com.netflix.eureka.http.jwt;

public interface IJWTInfo {
	/**
     * User name
     * @return
     */
    String getUniqueName();

    /**
     * User id
     * @return
     */
    String getId();

    /**
     * User meta data
     * @return
     */
    String getMetadata();
}
