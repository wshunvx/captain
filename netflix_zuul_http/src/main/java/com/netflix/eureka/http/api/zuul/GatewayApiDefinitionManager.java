package com.netflix.eureka.http.api.zuul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.SpiLoader;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.eureka.common.ApiDefinition;
import com.netflix.eureka.http.api.DefinitionChangeObserver;

public final class GatewayApiDefinitionManager {

    private static final Map<String, ApiDefinition> API_MAP = new ConcurrentHashMap<>();

    private static final ApiDefinitionPropertyListener LISTENER = new ApiDefinitionPropertyListener();
    private static SentinelProperty<List<ApiDefinition>> currentProperty = new DynamicSentinelProperty<>();

    /**
     * The map keeps all found ApiDefinitionChangeObserver (class name as key).
     */
    private static final Map<String, DefinitionChangeObserver> API_CHANGE_OBSERVERS = new ConcurrentHashMap<>();

    static {
        try {
            currentProperty.addListener(LISTENER);
            initializeApiChangeObserverSpi();
        } catch (Throwable ex) {
            RecordLog.warn("[GatewayApiDefinitionManager] Failed to initialize", ex);
            ex.printStackTrace();
        }
    }

    private static void initializeApiChangeObserverSpi() {
        List<DefinitionChangeObserver> listeners = SpiLoader.loadInstanceList(DefinitionChangeObserver.class);
        for (DefinitionChangeObserver e : listeners) {
            API_CHANGE_OBSERVERS.put(e.getClass().getCanonicalName(), e);
            RecordLog.info("[GatewayApiDefinitionManager] ApiDefinitionChangeObserver added: {}"
                , e.getClass().getCanonicalName());
        }
    }

    public static void register2Property(SentinelProperty<List<ApiDefinition>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[GatewayApiDefinitionManager] Registering new property to gateway API definition manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    /**
     * Load given gateway API definitions and apply to downstream observers.
     *
     * @param apiDefinitions list of gateway API definitions
     * @return true if updated, or else false
     */
    public static boolean loadApiDefinitions(List<ApiDefinition> apiDefinitions) {
        return currentProperty.updateValue(apiDefinitions);
    }

    public static ApiDefinition getApiDefinition(final String apiName) {
        if (apiName == null) {
            return null;
        }
        return API_MAP.get(apiName);
    }

    public static List<ApiDefinition> getApiDefinitions() {
        return new ArrayList<>(API_MAP.values());
    }

    private static final class ApiDefinitionPropertyListener implements PropertyListener<List<ApiDefinition>> {

        @Override
        public void configUpdate(List<ApiDefinition> list) {
            applyApiUpdateInternal(list);
            RecordLog.info("[GatewayApiDefinitionManager] Api definition updated: {}", API_MAP);
        }

        @Override
        public void configLoad(List<ApiDefinition> list) {
            applyApiUpdateInternal(list);
            RecordLog.info("[GatewayApiDefinitionManager] Api definition loaded: {}", API_MAP);
        }

        private static synchronized void applyApiUpdateInternal(List<ApiDefinition> list) {
            if (list == null || list.isEmpty()) {
                API_MAP.clear();
                notifyDownstreamListeners(new HashSet<ApiDefinition>());
                return;
            }
            Map<String, ApiDefinition> map = new HashMap<>(list.size());
            Set<ApiDefinition> validSet = new HashSet<>();
            for (ApiDefinition definition : list) {
                if (isValidApi(definition)) {
                    map.put(definition.getApiName(), definition);
                    validSet.add(definition);
                }
            }

            API_MAP.clear();
            API_MAP.putAll(map);

            // propagate to downstream.
            notifyDownstreamListeners(validSet);
        }
    }

    private static void notifyDownstreamListeners(/*@Valid*/ final Set<ApiDefinition> definitions) {
        try {
            for (Map.Entry<?, DefinitionChangeObserver> entry : API_CHANGE_OBSERVERS.entrySet()) {
                entry.getValue().onChange(definitions);
            }
        } catch (Exception ex) {
            RecordLog.warn("[GatewayApiDefinitionManager] WARN: failed to notify downstream api listeners", ex);
        }
    }

    public static boolean isValidApi(ApiDefinition apiDefinition) {
        return apiDefinition != null && StringUtil.isNotBlank(apiDefinition.getApiName())
            && apiDefinition.getPredicateItems() != null;
    }

    static void addApiChangeListener(DefinitionChangeObserver listener) {
        AssertUtil.notNull(listener, "listener cannot be null");
        API_CHANGE_OBSERVERS.put(listener.getClass().getCanonicalName(), listener);
    }

    static void removeApiChangeListener(Class<?> clazz) {
        AssertUtil.notNull(clazz, "class cannot be null");
        API_CHANGE_OBSERVERS.remove(clazz.getCanonicalName());
    }
}
