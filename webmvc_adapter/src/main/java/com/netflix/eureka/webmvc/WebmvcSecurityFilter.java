package com.netflix.eureka.webmvc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.netflix.eureka.webmvc.callback.BlockExceptionHandler;
import com.netflix.eureka.webmvc.config.WebmvcFilterConfig;
import com.netflix.eureka.webmvc.reactor.WebmvcInterceptor;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "eureka.security.enabled", matchIfMissing = true)
public class WebmvcSecurityFilter implements WebMvcConfigurer {

	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
		WebmvcFilterConfig config = new WebmvcFilterConfig();
        // config.setBlockExceptionHandler((request, response, e) -> { throw e; });
        // Use the default handler.
        config.setBlockExceptionHandler(new BlockExceptionHandler());

        // By default web context is true, means that unify web context(i.e. use the default context name),
        // If set it to false, entrance contexts will be separated by different URLs,
        config.setWebContextUnify(true);
        config.setOriginParser(request -> request.getHeader("User"));

        registry.addInterceptor(new WebmvcInterceptor(config)).addPathPatterns("/**");
    }
	
}
