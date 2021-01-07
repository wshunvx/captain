package com.netflix.eureka.zuul.cache;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.stereotype.Service;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import com.netflix.eureka.bean.ZUri;
import com.netflix.eureka.command.CommandConstants;
import com.netflix.eureka.http.auth.UriCache;
import com.netflix.eureka.http.auth.api.Cache;
import com.netflix.eureka.http.check.route.ZuulRouteMatchers;
import com.netflix.eureka.security.RsaKeyHelper;

@Service
public class AuthUriCacheImpl implements UriCache {

	private final ConcurrentHashMap<String, Map<String, Cache<ZUri>>> uris = new ConcurrentHashMap<String, Map<String, Cache<ZUri>>>();
	
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock read = readWriteLock.readLock();
    
    protected String password;
    private volatile KeyPair keyPair;
    
	@Override
	public void init(String password) {
		try {
			if(!(password == null || "".equals(password))) {
				if(!password.equals(this.password)) {
					keyPair = RsaKeyHelper.generateKey(password);
				}
			}
			this.password = password;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void invalidate(String svrid, long registrationTimestamp, Collection<ZUri> collection) {
		try {
			read.lock();
			Map<String, Cache<ZUri>> cache = uris.get(svrid);
			if(cache == null) {
				final ConcurrentHashMap<String, Cache<ZUri>> gNewMap = new ConcurrentHashMap<String, Cache<ZUri>>();
				cache = uris.putIfAbsent(svrid, gNewMap);
				if (cache == null) {
					cache = gNewMap;
				}
			}
			
			for(ZUri uri: collection) {
				Cache<ZUri> uriCache = cache.get(uri.getId());
				if(uriCache == null) {
					uriCache = new Cache<ZUri>(uri, registrationTimestamp);
				} else {
					if(uriCache.getRegistrationTimestamp() < registrationTimestamp) {
						uriCache.replace(uri, registrationTimestamp);
					}
					uriCache.renew();
				}
				cache.put(svrid, uriCache);
			}
		} finally {
			read.unlock();
		}
	}

	@Override
	public boolean verification(String svrid, String method, Route route) {
		Map<String, Cache<ZUri>> cache = uris.get(svrid);
		if(cache == null || cache.isEmpty()) {
			return false;
		}
		
		for(Cache<ZUri> c: cache.values()) {
			String iMethod = c.getUri().getMethod();
			if(!(iMethod == null || "".equals(iMethod))) {
				iMethod = method.toUpperCase();
				if(!iMethod.equals(iMethod)) {
					continue;
				}
			}
			Predicate<Route> matcher = fromApiPathPredicate(c.getUri());
			if (matcher.test(route)) {
                return true;
            }
		}
        return false;
	}

	@Override
	public KeyPair keyPair() {
		return keyPair;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	private Predicate<Route> fromApiPathPredicate(ZUri uri) {
		String pattern = uri.getBasepath();
        if (StringUtil.isBlank(pattern)) {
            return null;
        }
        switch (uri.getStrategy()) {
            case CommandConstants.URL_MATCH_STRATEGY_REGEX:
                return ZuulRouteMatchers.regexPath(pattern, false);
            case CommandConstants.URL_MATCH_STRATEGY_PREFIX:
                return ZuulRouteMatchers.antPath(pattern, false);
            default:
                return ZuulRouteMatchers.exactPath(pattern, false);
        }
    }
}
