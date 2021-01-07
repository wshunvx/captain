package com.netflix.eureka.http.check;

import java.util.Set;

import com.netflix.eureka.common.ApiDefinition;

public interface DefinitionChangeObserver {

    /**
     * Notify the observer about the new gateway API definitions.
     *
     * @param apiDefinitions new set of gateway API definition
     */
    void onChange(Set<ApiDefinition> apiDefinitions);
}
