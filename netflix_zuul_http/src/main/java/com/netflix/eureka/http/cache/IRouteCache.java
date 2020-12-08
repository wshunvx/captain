package com.netflix.eureka.http.cache;

import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;

public interface IRouteCache extends RouteLocator {
	void addRoute(String path, String location);
	void addRoute(ZuulRoute route);
	
	void refresh();
	
}
