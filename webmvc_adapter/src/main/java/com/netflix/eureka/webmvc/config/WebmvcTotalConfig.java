package com.netflix.eureka.webmvc.config;

public class WebmvcTotalConfig extends WebmvcConfig {

    public static final String DEFAULT_TOTAL_RESOURCE_NAME = "spring_mvc_total_url_request";
    public static final String DEFAULT_REQUEST_ATTRIBUTE_NAME = "$$spring_web_total_entry_attr";

    private String totalResourceName = DEFAULT_TOTAL_RESOURCE_NAME;

    public WebmvcTotalConfig() {
        super();
        setRequestAttributeName(DEFAULT_REQUEST_ATTRIBUTE_NAME);
    }

    public String getTotalResourceName() {
        return totalResourceName;
    }

    public WebmvcTotalConfig setTotalResourceName(String totalResourceName) {
        this.totalResourceName = totalResourceName;
        return this;
    }

    @Override
    public String toString() {
        return "SentinelWebMvcTotalConfig{" +
            "totalResourceName='" + totalResourceName + '\'' +
            ", requestAttributeName='" + requestAttributeName + '\'' +
            ", blockExceptionHandler=" + blockExceptionHandler +
            ", originParser=" + originParser +
            '}';
    }
}
