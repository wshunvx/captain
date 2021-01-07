package com.netflix.eureka.http.cache;

import java.util.Map;

import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;

public interface IRouteCache extends RouteLocator {
	Map<String, ZuulRoute> getLocateRoutes();
	
	void addRoute(String path, String location);
	void addRoute(ZuulRoute route);
	
	void delRoute(String path);
	
	void refresh();
	
}
