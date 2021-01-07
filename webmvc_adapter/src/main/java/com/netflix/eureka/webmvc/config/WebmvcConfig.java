package com.netflix.eureka.webmvc.config;

import com.netflix.eureka.webmvc.callback.BlockExceptionHandler;
import com.netflix.eureka.webmvc.callback.RequestOriginParser;

public abstract class WebmvcConfig {

    protected String requestAttributeName;
    protected String requestRefName;
    protected BlockExceptionHandler blockExceptionHandler;
    protected RequestOriginParser originParser;

    public String getRequestAttributeName() {
        return requestAttributeName;
    }

    public void setRequestAttributeName(String requestAttributeName) {
        this.requestAttributeName = requestAttributeName;
        this.requestRefName = this.requestAttributeName + "-rc";
    }
    
    /**
     * Paired with attr name used to track reference count.
     * 
     * @return
     */
    public String getRequestRefName() {
        return requestRefName;
    }

    public BlockExceptionHandler getBlockExceptionHandler() {
        return blockExceptionHandler;
    }

    public void setBlockExceptionHandler(BlockExceptionHandler blockExceptionHandler) {
        this.blockExceptionHandler = blockExceptionHandler;
    }

    public RequestOriginParser getOriginParser() {
        return originParser;
    }

    public void setOriginParser(RequestOriginParser originParser) {
        this.originParser = originParser;
    }
}
