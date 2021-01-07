package com.netflix.eureka.zuul.config;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.netflix.ribbon.support.RibbonRequestCustomizer;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonRoutingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.eureka.http.auth.ZuulServiceRunner;
import com.netflix.eureka.http.auth.UriCache;
import com.netflix.eureka.http.cache.IRouteCache;
import com.netflix.eureka.http.filters.ZuulErrorFilter;
import com.netflix.eureka.http.filters.ZuulPostFilter;
import com.netflix.eureka.zuul.filter.AccessGatewayFilter;
import com.netflix.eureka.zuul.filter.OkHttpRoutingFilter;
import com.netflix.eureka.zuul.msg.ZuulFallbackProvider;

@Configuration
public class GatewayConfig extends ZuulServiceRunner {
	
	@SuppressWarnings("rawtypes")
	@Autowired(required = false)
	private List<RibbonRequestCustomizer> requestCustomizers = Collections.emptyList();
	
	@Bean
	@ConditionalOnClass(IRouteCache.class)
	AccessGatewayFilter accessGatewayFilter(UriCache uriCache, IRouteCache routeLocator, ZuulProperties zuulProperties,
			@Value("${gate.ignore.prefix}") List<String> prefix) {
		return new AccessGatewayFilter(uriCache, routeLocator, zuulProperties, prefix);
	}
	
	@Bean RibbonRoutingFilter okHttpRoutingFilter(ProxyRequestHelper helper,
			RibbonCommandFactory<?> ribbonCommandFactory) {
		return new OkHttpRoutingFilter(helper, ribbonCommandFactory, this.requestCustomizers);
	}
	
	@Bean ZuulPostFilter getZuulPostFilter() {
        return new ZuulPostFilter();
    }
	
	@Bean ZuulFallbackProvider getZuulFallbackProvider() {
        return new ZuulFallbackProvider();
    }
	
	@Bean ZuulErrorFilter getZuulErrorFilter() {
        return new ZuulErrorFilter();
    }

}
