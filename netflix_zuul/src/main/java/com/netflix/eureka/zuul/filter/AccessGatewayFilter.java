package com.netflix.eureka.zuul.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netflix.eureka.http.filters.ZuulPreFilter;
import com.netflix.eureka.http.jwt.IJWTInfo;
import com.netflix.eureka.zuul.command.UserTokenCommand;
import com.netflix.eureka.zuul.jwt.JWTHelper;
import com.netflix.eureka.zuul.utils.RsaKeyHelper;
import com.netflix.eureka.zuul.utils.StringHelper;

public class AccessGatewayFilter extends ZuulPreFilter {
    
    private final int initialCapacity = 80;//初始队列大小
    private final int cacheAutoExpirationInSeconds = 360; //闲置时间(秒)
    
    private final LoadingCache<String, Role> loadingCache;
    

    public AccessGatewayFilter(RouteLocator routeLocator, ZuulProperties properties, List<String> prefix) {
		super(routeLocator, properties, prefix);
    	this.loadingCache =
                CacheBuilder.newBuilder().initialCapacity(initialCapacity)
                        .expireAfterAccess(cacheAutoExpirationInSeconds, TimeUnit.SECONDS)
                        .build(new CacheLoader<String, Role>() {
                            @Override
                            public Role load(String key) throws Exception {
                            	return generateRole(key);
                            }
                        });
    }
    
	private Role generateRole(String key) {
		return new Role(Arrays.asList("1300703206474518529", "1304245464733880321", "1304245464742268930", "1304245464742268931"));
	}

	@Override
	public IJWTInfo getJWTUser(String userToken, String serviceId, String serviceType) {
		return new UserTokenCommand(userToken, serviceId, serviceType) {
			
			@Override
			protected IJWTInfo fromToken(String token) throws Exception{
				Map<String, byte[]> keyMap = RsaKeyHelper.generateKey("fjw10vvz5q7sme36(WE45");
				return JWTHelper.getInfoFromToken(token, keyMap.get("pub"));
			}
			
			@Override
			protected Stream<String> fromPermissinon(IJWTInfo iJWTInfo, String svrid, String svrname) {
	        	return StringHelper.getObjectList(iJWTInfo.getMetadata()).stream().filter(role -> {
	        		Role ids = null;
					try {
						ids = loadingCache.get(role);
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					if(ids == null) {
	        			return false;
	        		}
	        		return true; //hasValue(svrid, svrname, new HashSet<String>(ids.getMenu()));
	        	});
			}
		}.execute();
	}

    public class Role {
        private final Collection<String> menu;

        public Role(Collection<String> menu) {
            this.menu = menu;
        }

		public Collection<String> getMenu() {
			return menu;
		}
    	
    }
}
