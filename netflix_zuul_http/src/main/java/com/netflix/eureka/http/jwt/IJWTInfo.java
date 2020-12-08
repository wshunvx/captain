package com.netflix.eureka.http.jwt;

public interface IJWTInfo {
	/**
     * 获取用户名
     * @return
     */
    String getUniqueName();

    /**
     * 获取用户ID
     * @return
     */
    String getId();

    /**
     * 获取权限
     * @return
     */
    String getMetadata();
}
