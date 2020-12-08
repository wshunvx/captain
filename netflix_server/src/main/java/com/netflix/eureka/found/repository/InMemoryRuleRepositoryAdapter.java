package com.netflix.eureka.found.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.netflix.eureka.dashboard.datasource.entity.rule.RuleEntity;
import com.netflix.eureka.dashboard.repository.rule.RuleRepository;

public abstract class InMemoryRuleRepositoryAdapter<T extends RuleEntity> implements RuleRepository<T> {
	/**
     * {@code <machine, <id, rule>>}
     */
	private final ConcurrentHashMap<String, Map<Long, T>> rules = new ConcurrentHashMap<String, Map<Long, T>>();

    @Override
    public void save(T entity) {
    	if (entity.getId() == null) {
            entity.setId(nextId());
        }
    	Map<Long, T> gMap = rules.get(entity.getApp());
        if (gMap == null) {
            final ConcurrentHashMap<Long, T> gNewMap = new ConcurrentHashMap<Long, T>();
            gMap = rules.putIfAbsent(entity.getApp(), gNewMap);
            if (gMap == null) {
                gMap = gNewMap;
            }
        }
        T existingLease = gMap.get(entity.getId());
        // Compare last update time, if there is already a gmt
        if (existingLease != null && (existingLease.getGmtCreate() != null)) {
            if (existingLease.getGmtCreate().before(entity.getGmtCreate())) {
                entity = existingLease;
            }
        }
        gMap.put(entity.getId(), entity);
    }

    @Override
    public void saveAll(List<T> rules) {
        if (rules != null) {
        	for (T rule : rules) {
            	save(rule);
            }
        }
    }

    @Override
    public T delete(String app, Long id) {
    	Map<Long, T> gMap = rules.get(app);
        T t = null;
        if (gMap != null) {
            t = gMap.remove(id);
        }
        return t;
    }

    @Override
    public T findById(String app, Long id) {
    	Map<Long, T> gMap = rules.get(app);
        T t = null;
        if (gMap != null) {
            t = gMap.get(id);
        }
        return t;
    }

    @Override
    public List<T> findAllByApp(String app) {
        Map<Long, T> entities = rules.get(app);
        if (entities == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(entities.values());
    }

    public void clearAll() {
    	rules.clear();
    }
    
    /**
     * Get next unused id.
     *
     * @return next unused id
     */
    abstract protected Long nextId();

}
