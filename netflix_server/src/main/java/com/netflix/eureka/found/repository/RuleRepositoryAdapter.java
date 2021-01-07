package com.netflix.eureka.found.repository;

import java.util.Collection;
import java.util.List;

import com.netflix.eureka.command.Resource;

public abstract class RuleRepositoryAdapter<T> {
    
    /**
     * Get next unused id.
     *
     * @return next unused id
     */
    abstract public Long nextId();
    
    abstract public T getRule(Resource key, Long id);
    abstract public Collection<T> getRule(Resource key);
    
    abstract public boolean setRule(Resource key, T t);
    abstract public boolean setRule(Resource key, List<T> values);
    
    abstract public boolean removeRule(Resource key, Long id);
    abstract public boolean removeRule(Resource key, T t);
    abstract public Collection<T> removeRule(Resource key);

}
