package com.netflix.eureka.http.jwt;

public interface IJWTInfo {
	/**
     * ��ȡ�û���
     * @return
     */
    String getUniqueName();

    /**
     * ��ȡ�û�ID
     * @return
     */
    String getId();

    /**
     * ��ȡȨ��
     * @return
     */
    String getMetadata();
}
