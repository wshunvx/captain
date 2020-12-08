package com.netflix.eureka.http.common;

public interface RequestItemParser<T> {

    /**
     * Get API path from the request.
     *
     * @param request valid request
     * @return API path
     */
    String getPath(T request);

    /**
     * Get remote address from the request.
     *
     * @param request valid request
     * @return remote address
     */
    String getRemoteAddress(T request);

    /**
     * Get the header associated with the header key.
     *
     * @param request valid request
     * @param key     valid header key
     * @return the header
     */
    String getHeader(T request, String key);

    /**
     * Get the parameter value associated with the parameter name.
     *
     * @param request   valid request
     * @param paramName valid parameter name
     * @return the parameter value
     */
    String getUrlParam(T request, String paramName);

    /**
     * Get the cookie value associated with the cookie name.
     *
     * @param request    valid request
     * @param cookieName valid cookie name
     * @return the cookie value
     * @since 1.7.0
     */
    String getCookieValue(T request, String cookieName);
}
