package com.netflix.eureka.found.repository;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.netflix.eureka.command.Resource;
import com.netflix.eureka.constid.SnowflakeIdWorker;
import com.netflix.eureka.dashboard.datasource.entity.rule.RuleEntity;

@Service
public class InMemoryRuleRepositoryAdapter<T extends RuleEntity> extends RuleRepositoryAdapter<T> {
	private static final Logger logger = LoggerFactory.getLogger(RuleRepositoryAdapter.class);
	
	private static SnowflakeIdWorker ids = SnowflakeIdWorker.newID(System.currentTimeMillis(), 1, 0);
	
    private static final int EXPIRATION_IN_SECONDS = 500; //闲置时间
    private static final int INITIAL_CAPACITY_OF_RESPONSE_CACHE = 30;
    
	private final LoadingCache<Resource, Collection<T>> readWriteCacheMap;
    
	/**
	 *  <h3>Storage format:</h3>
	 *
	 * <p>Resource(FLOW_RULE_TYPE, DEGRADE_RULE_TYPE, SYSTEM_RULE_TYPE, AUTHORITY_RULE_TYPE): <pre>   {@code
	 *
	 *   Multimap<String, RuleEntity> multimap = ListMultimap<>;
	 *   
	 *   }</pre>
	 */
	private final Multimap<Resource, T> regionSpecificKeys =
            Multimaps.newListMultimap(new ConcurrentHashMap<Resource, Collection<T>>(), new Supplier<List<T>>() {
                @Override
                public List<T> get() {
                    return new CopyOnWriteArrayList<T>();
                }
            });
	
	public InMemoryRuleRepositoryAdapter() {
		/**
		 * Get rid of the old。
		 * logic...
		 * Not implemented!!!
		 */
		readWriteCacheMap = CacheBuilder.newBuilder().initialCapacity(INITIAL_CAPACITY_OF_RESPONSE_CACHE)
        .expireAfterWrite(EXPIRATION_IN_SECONDS, TimeUnit.SECONDS)
//        .removalListener(new RemovalListener<Resource, T>() {
//            @Override
//            public void onRemoval(RemovalNotification<Resource, T> notification) {
//                // Region Specific Keys (Update collection).
//            }
//        })
        .build(new CacheLoader<Resource, Collection<T>>() {
            @Override
            public Collection<T> load(Resource key) throws Exception {
            	return regionSpecificKeys.get(key);
            }
        });
	}
	
	@Override
	public Long nextId() {
		return ids.nextId();
	}

	@Override
	public boolean setRule(Resource key, T t) {
		if(StringUtils.isEmpty(t.getId())) {
			t.setId(nextId());
		}
		return regionSpecificKeys.put(key, t);
	}
	
    @Override
	public boolean setRule(Resource key, List<T> values) {
    	return regionSpecificKeys.putAll(key, values.stream().map(t -> {
    		if(StringUtils.isEmpty(t.getId())) {
				t.setId(nextId());
			}
    		return t;
    	}).collect(Collectors.toList()));
	}

	@Override
    public T getRule(Resource key, Long id) {
    	Collection<T> value = getValue(key);
    	if(value == null) {
    		return null;
    	}
    	List<T> list = value.stream().filter(v -> v.getId() != id).collect(Collectors.toList());
    	if(list.isEmpty()) {
    		return null;
    	}
    	return list.get(0);
	}

	@Override
	public Collection<T> getRule(Resource key) {
		return getValue(key);
	}

	@Override
	public boolean removeRule(Resource key, Long id) {
		T t = getRule(key, id);
		if(t == null) {
			return false;
		}
		return regionSpecificKeys.remove(key, t);
	}

	@Override
	public boolean removeRule(Resource key, T t) {
		return regionSpecificKeys.remove(key, t);
	}

	@Override
	public Collection<T> removeRule(Resource key) {
		return regionSpecificKeys.removeAll(key);
	}

    Collection<T> get(final Resource key) {
        return getValue(key);
    }
    
    Collection<T> getValue(final Resource key) {
    	Collection<T> t = null;
        try {
        	t = readWriteCacheMap.get(key);
        } catch (Throwable e) {
            logger.error("Cannot get value for key : {}", key, e);
        }
        return t;
    }
    
}
