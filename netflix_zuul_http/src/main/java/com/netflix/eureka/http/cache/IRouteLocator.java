package com.netflix.eureka.http.cache;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;

public class IRouteLocator extends SimpleRouteLocator implements IRouteCache {
	private static Logger log = LoggerFactory.getLogger(IRouteLocator.class);
	
	private ZuulProperties properties;
	
	public IRouteLocator(ServerProperties servers, ZuulProperties properties) {
		super(servers.getServlet().getContextPath(), properties);
		this.properties = properties;
	}
	
	@Override
	public Map<String, ZuulRoute> getLocateRoutes() {
		return locateRoutes();
	}

	public void addRoute(String path, String location) {
		this.properties.getRoutes().put(path, new ZuulRoute(path, location));
		refresh();
	}

	public void addRoute(ZuulRoute route) {
		this.properties.getRoutes().put(route.getPath(), route);
		refresh();
	}
	
	@Override
	public void delRoute(String path) {
		this.properties.getRoutes().remove(path);
		refresh();
	}

	protected void addConfiguredRoutes(Map<String, ZuulRoute> routes) {
		Map<String, ZuulRoute> routeEntries = this.properties.getRoutes();
		for (ZuulRoute entry : routeEntries.values()) {
			String route = entry.getPath();
			if (routes.containsKey(route)) {
				log.warn("Overwriting route " + route + ": already defined by "
						+ routes.get(route));
			}
			routes.put(route, entry);
		}
	}

	@Override
	public void refresh() {
		doRefresh();
	}
}
