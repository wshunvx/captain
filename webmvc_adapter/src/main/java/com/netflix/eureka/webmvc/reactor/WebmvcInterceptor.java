package com.netflix.eureka.webmvc.reactor;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.HandlerMapping;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.eureka.webmvc.callback.UrlCleaner;
import com.netflix.eureka.webmvc.config.WebmvcFilterConfig;

public class WebmvcInterceptor extends AbstractInterceptor {

    private final WebmvcFilterConfig config;

    public WebmvcInterceptor() {
        this(new WebmvcFilterConfig());
    }

    public WebmvcInterceptor(WebmvcFilterConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    protected String getResourceName(HttpServletRequest request) {
        // Resolve the Spring Web URL pattern from the request attribute.
        Object resourceNameObject = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (resourceNameObject == null || !(resourceNameObject instanceof String)) {
            return null;
        }
        String resourceName = (String) resourceNameObject;
        UrlCleaner urlCleaner = config.getUrlCleaner();
        if (urlCleaner != null) {
            resourceName = urlCleaner.clean(resourceName);
        }
        // Add method specification if necessary
        if (StringUtil.isNotEmpty(resourceName) && config.isHttpMethodSpecify()) {
            resourceName = request.getMethod().toUpperCase() + ":" + resourceName;
        }
        return resourceName;
    }

    @Override
    protected String getContextName(HttpServletRequest request) {
        if (config.isWebContextUnify()) {
            return super.getContextName(request);
        }

        return getResourceName(request);
    }
}
