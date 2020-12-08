package com.netflix.eureka.dashboard.repository.rule;

import java.util.List;

public interface RuleRepository<T> {

    /**
     * Save one.
     *
     * @param entity
     * @return
     */
    void save(T entity);

    /**
     * Save all.
     *
     * @param rules
     * @return rules saved.
     */
    void saveAll(List<T> rules);

    /**
     * Delete by id
     *
     * @param id
     * @return entity deleted
     */
    T delete(String app, Long id);

    /**
     * Find by id.
     *
     * @param id
     * @return
     */
    T findById(String app, Long id);

    /**
     * Find all by application.
     *
     * @param appName valid app name
     * @return all rules of the application
     */
    List<T> findAllByApp(String app);

    ///**
    // * Find all by app and enable switch.
    // * @param app
    // * @param enable
    // * @return
    // */
    //List<T> findAllByAppAndEnable(String app, boolean enable);
}
