package com.netflix.eureka.webmvc.reactor;

import javax.servlet.http.HttpServletRequest;

import com.netflix.eureka.webmvc.config.WebmvcTotalConfig;

public class WebmvcTotalInterceptor extends AbstractInterceptor {

    private final WebmvcTotalConfig config;

    public WebmvcTotalInterceptor(WebmvcTotalConfig config) {
        super(config);
        if (config == null) {
            this.config = new WebmvcTotalConfig();
        } else {
            this.config = config;
        }
    }

    public WebmvcTotalInterceptor() {
        this(new WebmvcTotalConfig());
    }

    @Override
    protected String getResourceName(HttpServletRequest request) {
        return config.getTotalResourceName();
    }
}
