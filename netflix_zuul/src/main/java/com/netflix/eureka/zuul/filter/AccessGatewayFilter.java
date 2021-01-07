package com.netflix.eureka.zuul.filter;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.netflix.hystrix.HystrixCircuitBreakerFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.util.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netflix.eureka.http.auth.UriCache;
import com.netflix.eureka.http.cache.IRouteCache;
import com.netflix.eureka.http.filters.ZuulPreFilter;
import com.netflix.eureka.http.jwt.IJWTInfo;
import com.netflix.eureka.zuul.jwt.JWTHelper;
import com.netflix.eureka.zuul.utils.StringHelper;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

public class AccessGatewayFilter extends ZuulPreFilter {
    private static Logger log = LoggerFactory.getLogger(AccessGatewayFilter.class);
    private final int initialCapacity = 80;//初始队列大小
    private final int cacheAutoExpirationInSeconds = 360; //闲置时间(秒)
    
    private final LoadingCache<String, Role> loadingCache;
    
    private final Function<String, HystrixCommand.Setter> defaultConfiguration = id -> HystrixCommand.Setter
			.withGroupKey(
					HystrixCommandGroupKey.Factory.asKey(getClass().getSimpleName()))
			.andCommandKey(HystrixCommandKey.Factory.asKey(id))
			.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
					// 设置超时时间(以毫秒为单位), 在此时间之后, 调用方将观察超时并退出命令执行.
					.withExecutionTimeoutInMilliseconds(1000) // 默认1000
//					// 在发生故障或拒绝时是否尝试调用
//					.withFallbackEnabled(true)// 默认True
//					// 是否启用超时设置
//					.withExecutionTimeoutEnabled(true) // 默认True
//					// 超时发生时是否应中断执行
//					.withExecutionIsolationThreadInterruptOnTimeout(true) // 默认True
//					// 当线程执行超时, 是否进行中断处理, 即异步的Future#cancel()
//					.withExecutionIsolationThreadInterruptOnFutureCancel(true)// 默认False
//					// 允许最大请求数, 如果达到此最大并发限制, 则后续请求将被拒绝.
//					.withExecutionIsolationSemaphoreMaxConcurrentRequests(30)// 默认10
//					// 允许调用线程发出的最大请求数, 如果达到最大并发限制, 则后续请求将被拒绝, 并引发异常.
//					.withFallbackIsolationSemaphoreMaxConcurrentRequests(30)// 默认10
//					// 采样请求数, 一个采样周期内必须进行至少多个请求才能进行采样统计
//					.withCircuitBreakerRequestVolumeThreshold(20)// 默认20
//					// 采样容错率(百分比), 失败率超过该配置, 则打开熔断开关
//					.withCircuitBreakerErrorThresholdPercentage(50)// 默认50
//					// 当断路器拒绝请求期间, 等待多久会再次执行请求
//					.withCircuitBreakerSleepWindowInMilliseconds(5000)// 默认5000
//					// 存储桶统计持续时间
//					.withMetricsRollingStatisticalWindowInMilliseconds(10000)// 默认10000
//					// 存储桶持续时间内分配桶的数量, 计算方式 10000(10second) / 10 = 1/second
//					.withMetricsRollingStatisticalWindowBuckets(10)// 默认10
//					// 存储桶保留执行时间, 用于计算响应统计
//					.withMetricsRollingPercentileWindowInMilliseconds(60000)// 默认60000
//					// 存储桶执行时间内分配桶的数量, 计算方式 60000(6second) / 6 = 1/second
//					.withMetricsRollingPercentileWindowBuckets(6)// 默认6
//					// 每个存储桶保留最大执行时间
//					.withMetricsRollingPercentileBucketSize(100)// 默认100
//					// 存储桶统计间隔
//					.withMetricsHealthSnapshotIntervalInMilliseconds(500)// 默认500
				);
    private final CircuitBreakerFactory<HystrixCommand.Setter, HystrixCircuitBreakerFactory.HystrixConfigBuilder> command;
    
    public AccessGatewayFilter(UriCache uriCache, IRouteCache routeLocator, ZuulProperties properties, List<String> prefix) {
		super(uriCache, routeLocator, properties, prefix);
    	this.loadingCache =
                CacheBuilder.newBuilder().initialCapacity(initialCapacity)
                        .expireAfterAccess(cacheAutoExpirationInSeconds, TimeUnit.SECONDS)
                        .build(new CacheLoader<String, Role>() {
                            @Override
                            public Role load(String key) throws Exception {
                            	return generateRole(key);
                            }
                        });
    	this.command = new HystrixCircuitBreakerFactory();
    	this.command.configureDefault(defaultConfiguration);
    }
    
	private Role generateRole(String key) {
		return new Role(Arrays.asList("1300703206474518529", "1304245464733880321", "1304245464742268930", "1304245464742268931"));
	}

	@Override
	public IJWTInfo getJWTUser(KeyPair keyMap, String userToken) {
		return command.create("jwt__user-token").run(() -> {
			try {
				IJWTInfo iJWTInfo = fromToken(keyMap, userToken);
				if(iJWTInfo == null || iJWTInfo.getMetadata() == null) {
					return null;
				}
				
				Stream<String> permissinon = fromPermissinon(iJWTInfo);
				if(permissinon == null || permissinon.count() == 0) {
					return null;
				}
				
				return iJWTInfo;
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
			return null;
		}, throwable -> {
			throw new RuntimeException("Token not recognized.");
		});
		
	}
	
	protected IJWTInfo fromToken(KeyPair keyMap, String token) throws Exception {
		if(StringUtils.isEmpty(token)) {
			throw new RuntimeException("User Token undefined.");
		}
		
		if(keyMap == null) {
			throw new RuntimeException("Rsa key be overdue.");
		}
		
		return JWTHelper.getInfoFromToken(token, keyMap.getPublic().getEncoded());
	}

	protected Stream<String> fromPermissinon(IJWTInfo iJWTInfo) {
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
