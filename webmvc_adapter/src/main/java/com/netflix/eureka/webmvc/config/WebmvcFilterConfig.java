package com.netflix.eureka.webmvc.config;

import com.netflix.eureka.webmvc.callback.UrlCleaner;

public class WebmvcFilterConfig extends WebmvcConfig {

    public static final String DEFAULT_REQUEST_ATTRIBUTE_NAME = "$$spring_web_entry_attr";

    /**
     * Specify the URL cleaner that unifies the URL resources.
     */
    private UrlCleaner urlCleaner;

    /**
     * Specify whether the URL resource name should contain the HTTP method prefix (e.g. {@code POST:}).
     */
    private boolean httpMethodSpecify;

    /**
     * Specify whether unify web context(i.e. use the default context name), and is true by default.
     *
     * @since 1.7.2
     */
    private boolean webContextUnify = false;

    public WebmvcFilterConfig() {
        super();
        setRequestAttributeName(DEFAULT_REQUEST_ATTRIBUTE_NAME);
    }

    public UrlCleaner getUrlCleaner() {
        return urlCleaner;
    }

    public WebmvcFilterConfig setUrlCleaner(UrlCleaner urlCleaner) {
        this.urlCleaner = urlCleaner;
        return this;
    }

    public boolean isHttpMethodSpecify() {
        return httpMethodSpecify;
    }

    public WebmvcFilterConfig setHttpMethodSpecify(boolean httpMethodSpecify) {
        this.httpMethodSpecify = httpMethodSpecify;
        return this;
    }

    public boolean isWebContextUnify() {
        return webContextUnify;
    }

    public WebmvcFilterConfig setWebContextUnify(boolean webContextUnify) {
        this.webContextUnify = webContextUnify;
        return this;
    }

    @Override
    public String toString() {
        return "SentinelWebMvcConfig{" +
            "urlCleaner=" + urlCleaner +
            ", httpMethodSpecify=" + httpMethodSpecify +
            ", webContextUnify=" + webContextUnify +
            ", requestAttributeName='" + requestAttributeName + '\'' +
            ", blockExceptionHandler=" + blockExceptionHandler +
            ", originParser=" + originParser +
            '}';
    }
}
