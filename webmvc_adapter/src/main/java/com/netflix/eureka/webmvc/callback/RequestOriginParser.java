package com.netflix.eureka.webmvc.callback;

import javax.servlet.http.HttpServletRequest;

public interface RequestOriginParser {

    String parseOrigin(HttpServletRequest request);
}
