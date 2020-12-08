package com.netflix.eureka.found.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Tuple2;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.domain.cluster.ClusterAppAssignResultVO;
import com.netflix.eureka.dashboard.domain.cluster.ClusterGroupEntity;
import com.netflix.eureka.dashboard.domain.cluster.config.ClusterClientConfig;
import com.netflix.eureka.dashboard.domain.cluster.config.ServerFlowConfig;
import com.netflix.eureka.dashboard.domain.cluster.config.ServerTransportConfig;
import com.netflix.eureka.dashboard.domain.cluster.request.ClusterAppAssignMap;
import com.netflix.eureka.dashboard.domain.cluster.request.ClusterClientModifyRequest;
import com.netflix.eureka.dashboard.domain.cluster.request.ClusterServerModifyRequest;
import com.netflix.eureka.dashboard.domain.cluster.state.ClusterClientStateVO;
import com.netflix.eureka.dashboard.domain.cluster.state.ClusterUniversalStatePairVO;
import com.netflix.eureka.dashboard.domain.cluster.state.ClusterUniversalStateVO;
import com.netflix.eureka.dashboard.service.ClusterService;
import com.netflix.eureka.dashboard.util.AsyncUtils;
import com.netflix.eureka.dashboard.util.ClusterEntityUtils;
import com.netflix.eureka.dashboard.util.MachineUtils;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

public class ClusterServiceImpl implements ClusterService {

    private final Logger LOGGER = LoggerFactory.getLogger(ClusterServiceImpl.class);

    private SentinelApiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    
    public ClusterServiceImpl(SentinelApiClient sentinelApiClient, PeerAwareInstanceRegistry instanceRegistry) {
    	this.sentinelApiClient = sentinelApiClient;
    	this.instanceRegistry = instanceRegistry;
    }
    
    private boolean isMachineInApp(/*@NonEmpty*/ String machineId) {
        return machineId.contains(":");
    }

    private ClusterAppAssignResultVO handleUnbindClusterServerNotInApp(String app, String machineId) {
        Set<String> failedSet = new HashSet<>();
        try {
            List<ClusterUniversalStatePairVO> list = getClusterUniversalState(app)
                .get(10, TimeUnit.SECONDS);
            Set<String> toModifySet = list.stream()
                .filter(e -> e.getState().getStateInfo().getMode() == ClusterStateManager.CLUSTER_CLIENT)
                .filter(e -> machineId.equals(e.getState().getClient().getClientConfig().getServerHost() + ':' +
                    e.getState().getClient().getClientConfig().getServerPort()))
                .map(e -> e.getIp() + '@' + e.getCommandPort())
                .collect(Collectors.toSet());
            // Modify mode to NOT-STARTED for all associated token clients.
            modifyToNonStarted(toModifySet, failedSet);
        } catch (Exception ex) {
            Throwable e = ex instanceof ExecutionException ? ex.getCause() : ex;
            LOGGER.error("Failed to unbind machine <{}>", machineId, e);
            failedSet.add(machineId);
        }
        return new ClusterAppAssignResultVO()
            .setFailedClientSet(failedSet)
            .setFailedServerSet(new HashSet<>());
    }

    private void modifyToNonStarted(Set<String> toModifySet, Set<String> failedSet) {
        toModifySet.parallelStream()
            .map(MachineUtils::parseCommandIpAndPort)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(e -> {
            	String homePage = "http://" + e.r1 + ":" + e.r2 + "/";
                CompletableFuture<Void> f = modifyMode(homePage, ClusterStateManager.CLUSTER_NOT_STARTED);
                return Tuple2.of(e.r1 + '@' + e.r2, f);
            })
            .forEach(f -> handleFutureSync(f, failedSet));
    }

    @Override
    public ClusterAppAssignResultVO unbindClusterServer(String app, String machineId) {
        AssertUtil.assertNotBlank(app, "app cannot be blank");
        AssertUtil.assertNotBlank(machineId, "machineId cannot be blank");

        if (isMachineInApp(machineId)) {
            return handleUnbindClusterServerNotInApp(app, machineId);
        }
        Set<String> failedSet = new HashSet<>();
        try {
            ClusterGroupEntity entity = getClusterUniversalStateForAppMachine(app, machineId)
                .get(10, TimeUnit.SECONDS);
            Set<String> toModifySet = new HashSet<>();
            toModifySet.add(machineId);
            if (entity.getClientSet() != null) {
                toModifySet.addAll(entity.getClientSet());
            }
            // Modify mode to NOT-STARTED for all chosen token servers and associated token clients.
            modifyToNonStarted(toModifySet, failedSet);
        } catch (Exception ex) {
            Throwable e = ex instanceof ExecutionException ? ex.getCause() : ex;
            LOGGER.error("Failed to unbind machine <{}>", machineId, e);
            failedSet.add(machineId);
        }
        return new ClusterAppAssignResultVO()
            .setFailedClientSet(failedSet)
            .setFailedServerSet(new HashSet<>());
    }

    @Override
    public ClusterAppAssignResultVO unbindClusterServers(String app, Set<String> machineIdSet) {
        AssertUtil.assertNotBlank(app, "app cannot be blank");
        AssertUtil.isTrue(machineIdSet != null && !machineIdSet.isEmpty(), "machineIdSet cannot be empty");
        ClusterAppAssignResultVO result = new ClusterAppAssignResultVO()
            .setFailedClientSet(new HashSet<>())
            .setFailedServerSet(new HashSet<>());
        for (String machineId : machineIdSet) {
            ClusterAppAssignResultVO resultVO = unbindClusterServer(app, machineId);
            result.getFailedClientSet().addAll(resultVO.getFailedClientSet());
            result.getFailedServerSet().addAll(resultVO.getFailedServerSet());
        }
        return result;
    }

    @Override
    public ClusterAppAssignResultVO applyAssignToApp(String app, List<ClusterAppAssignMap> clusterMap,
                                                     Set<String> remainingSet) {
        AssertUtil.assertNotBlank(app, "app cannot be blank");
        AssertUtil.notNull(clusterMap, "clusterMap cannot be null");
        Set<String> failedServerSet = new HashSet<>();
        Set<String> failedClientSet = new HashSet<>();

        // Assign server and apply config.
        clusterMap.stream()
            .filter(Objects::nonNull)
            .filter(ClusterAppAssignMap::getBelongToApp)
            .map(e -> {
                String homePage = "http://" + e.getIp() + ":" + parsePort(e) + "/";
                CompletableFuture<Void> f = modifyMode(homePage, ClusterStateManager.CLUSTER_SERVER)
                    .thenCompose(v -> applyServerConfigChange(app, homePage, e));
                return Tuple2.of(e.getMachineId(), f);
            })
            .forEach(t -> handleFutureSync(t, failedServerSet));

        // Assign client of servers and apply config.
        clusterMap.parallelStream()
            .filter(Objects::nonNull)
            .forEach(e -> applyAllClientConfigChange(app, e, failedClientSet));

        // Unbind remaining (unassigned) machines.
        applyAllRemainingMachineSet(app, remainingSet, failedClientSet);

        return new ClusterAppAssignResultVO()
            .setFailedClientSet(failedClientSet)
            .setFailedServerSet(failedServerSet);
    }
    
    @Override
    public CompletableFuture<Void> modifyClusterClientConfig(ClusterClientModifyRequest request) {
        if (notClientRequestValid(request)) {
            throw new IllegalArgumentException("Invalid request");
        }
        
        InstanceInfo instanceInfo = null;
        Application application = instanceRegistry.getApplication(request.getApp());
        if(application != null) {
        	instanceInfo = application.getByInstanceId(request.getId());
        }
        if(instanceInfo == null) {
        	throw new IllegalArgumentException("instanceInfo set cannot be null");
        }
        String homePage = instanceInfo.getHomePageUrl();
        return sentinelApiClient.modifyClusterClientConfig(request.getApp(), homePage, request.getClientConfig())
            .thenCompose(v -> sentinelApiClient.modifyClusterMode(homePage, ClusterStateManager.CLUSTER_CLIENT));
    }
    
    @Override
    public CompletableFuture<Void> modifyClusterServerConfig(ClusterServerModifyRequest request) {
        ServerTransportConfig transportConfig = request.getTransportConfig();
        ServerFlowConfig flowConfig = request.getFlowConfig();
        Set<String> namespaceSet = request.getNamespaceSet();
        if (invalidTransportConfig(transportConfig)) {
            throw new IllegalArgumentException("Invalid transport config in request");
        }
        if (invalidFlowConfig(flowConfig)) {
            throw new IllegalArgumentException("Invalid flow config in request");
        }
        if (namespaceSet == null) {
            throw new IllegalArgumentException("namespace set cannot be null");
        }
        InstanceInfo instanceInfo = null;
        Application application = instanceRegistry.getApplication(request.getApp());
        if(application != null) {
        	instanceInfo = application.getByInstanceId(request.getId());
        }
        if(instanceInfo == null) {
        	throw new IllegalArgumentException("instanceInfo set cannot be null");
        }
        String app = request.getApp();
        String homePage = instanceInfo.getHomePageUrl();
        return sentinelApiClient.modifyClusterServerNamespaceSet(app, homePage, namespaceSet)
            .thenCompose(v -> sentinelApiClient.modifyClusterServerTransportConfig(app, homePage, transportConfig))
            .thenCompose(v -> sentinelApiClient.modifyClusterServerFlowConfig(app, homePage, flowConfig))
            .thenCompose(v -> sentinelApiClient.modifyClusterMode(homePage, ClusterStateManager.CLUSTER_SERVER));
    }

    /**
     * Get cluster state list of all available machines of provided application.
     *
     * @param app application name
     * @return cluster state list of all available machines of the application
     */
    @Override
    public CompletableFuture<List<ClusterUniversalStatePairVO>> getClusterUniversalState(String app) {
        if (StringUtil.isBlank(app)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("app cannot be empty"));
        }
        Application appInfo = instanceRegistry.getApplication(app);
        if (appInfo == null || appInfo.getInstances() == null) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        List<CompletableFuture<ClusterUniversalStatePairVO>> futures =  appInfo.getInstances().stream()
            .filter(e -> e.getStatus() == InstanceStatus.UP)
            .map(machine -> getClusterUniversalState(app, machine.getHomePageUrl())
            		.thenApply(e -> new ClusterUniversalStatePairVO(machine.getInstanceId(), machine.getPort(), e)))
            .collect(Collectors.toList());
        return AsyncUtils.sequenceSuccessFuture(futures);
    }

    @Override
    public CompletableFuture<ClusterGroupEntity> getClusterUniversalStateForAppMachine(String app, String machineId) {
        if (StringUtil.isBlank(app)) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("app cannot be empty"));
        }
        Application appInfo = instanceRegistry.getApplication(app);
        if (appInfo == null || appInfo.getInstances() == null) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("app does not have machines"));
        }

        boolean machineOk = appInfo.getInstances().stream()
            .filter(e -> e.getStatus() == InstanceStatus.UP)
            .map(e -> e.getInstanceId() + '@' + e.getPort())
            .anyMatch(e -> e.equals(machineId));
        if (!machineOk) {
            return AsyncUtils.newFailedFuture(new IllegalStateException("machine does not exist or disconnected"));
        }

        return getClusterUniversalState(app)
            .thenApply(ClusterEntityUtils::wrapToClusterGroup)
            .thenCompose(e -> e.stream()
                .filter(e1 -> e1.getMachineId().equals(machineId))
                .findAny()
                .map(CompletableFuture::completedFuture)
                .orElse(AsyncUtils.newFailedFuture(new IllegalStateException("not a server: " + machineId)))
            );
    }

    @Override
    public CompletableFuture<ClusterUniversalStateVO> getClusterUniversalState(String app, String homePage) {
        return sentinelApiClient.fetchClusterMode(homePage)
            .thenApply(e -> new ClusterUniversalStateVO().setStateInfo(e))
            .thenCompose(vo -> {
                if (vo.getStateInfo().getClientAvailable()) {
                    return sentinelApiClient.fetchClusterClientInfoAndConfig(homePage)
                        .thenApply(cc -> vo.setClient(new ClusterClientStateVO().setClientConfig(cc)));
                } else {
                    return CompletableFuture.completedFuture(vo);
                }
            }).thenCompose(vo -> {
                if (vo.getStateInfo().getServerAvailable()) {
                    return sentinelApiClient.fetchClusterServerBasicInfo(homePage)
                        .thenApply(vo::setServer);
                } else {
                    return CompletableFuture.completedFuture(vo);
                }
            });
    }

    private boolean notClientRequestValid(/*@NonNull */ ClusterClientModifyRequest request) {
        ClusterClientConfig config = request.getClientConfig();
        return config == null || StringUtil.isEmpty(config.getServerHost())
            || config.getServerPort() == null || config.getServerPort() <= 0
            || config.getRequestTimeout() == null || config.getRequestTimeout() <= 0;
    }

    

    private boolean invalidTransportConfig(ServerTransportConfig transportConfig) {
        return transportConfig == null || transportConfig.getPort() == null || transportConfig.getPort() <= 0
            || transportConfig.getIdleSeconds() == null || transportConfig.getIdleSeconds() <= 0;
    }

    private boolean invalidFlowConfig(ServerFlowConfig flowConfig) {
        return flowConfig == null || flowConfig.getSampleCount() == null || flowConfig.getSampleCount() <= 0
            || flowConfig.getIntervalMs() == null || flowConfig.getIntervalMs() <= 0
            || flowConfig.getIntervalMs() % flowConfig.getSampleCount() != 0
            || flowConfig.getMaxAllowedQps() == null || flowConfig.getMaxAllowedQps() < 0;
    }
    
    private void applyAllRemainingMachineSet(String app, Set<String> remainingSet, Set<String> failedSet) {
        if (remainingSet == null || remainingSet.isEmpty()) {
            return;
        }
        remainingSet.parallelStream()
            .filter(Objects::nonNull)
            .map(MachineUtils::parseCommandIpAndPort)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(ipPort -> {
                String homePage = "http://" + ipPort.r1 + ":" + ipPort.r2 + "/";
                CompletableFuture<Void> f = modifyMode(homePage, ClusterStateManager.CLUSTER_NOT_STARTED);
                return Tuple2.of(ipPort.r1 + '@' + ipPort.r2, f);
            })
            .forEach(t -> handleFutureSync(t, failedSet));
    }

    private void applyAllClientConfigChange(String app, ClusterAppAssignMap assignMap,
                                            Set<String> failedSet) {
        Set<String> clientSet = assignMap.getClientSet();
        if (clientSet == null || clientSet.isEmpty()) {
            return;
        }
        final String serverIp = assignMap.getIp();
        final int serverPort = assignMap.getPort();
        clientSet.stream()
            .map(MachineUtils::parseCommandIpAndPort)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(ipPort -> {
            	String homePage = "http://" + ipPort.r1 + ":" + ipPort.r2 + "/";
                CompletableFuture<Void> f = sentinelApiClient
                    .modifyClusterMode(homePage, ClusterStateManager.CLUSTER_CLIENT)
                    .thenCompose(v -> sentinelApiClient.modifyClusterClientConfig(app, homePage,
                        new ClusterClientConfig().setRequestTimeout(20)
                            .setServerHost(serverIp)
                            .setServerPort(serverPort)
                    ));
                return Tuple2.of(ipPort.r1 + '@' + ipPort.r2, f);
            })
            .forEach(t -> handleFutureSync(t, failedSet));
    }

    private void handleFutureSync(Tuple2<String, CompletableFuture<Void>> t, Set<String> failedSet) {
        try {
            t.r2.get(10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            if (ex instanceof ExecutionException) {
                LOGGER.error("Request for <{}> failed", t.r1, ex.getCause());
            } else {
                LOGGER.error("Request for <{}> failed", t.r1, ex);
            }
            failedSet.add(t.r1);
        }
    }

    private CompletableFuture<Void> applyServerConfigChange(String app, String homePage,
                                                            ClusterAppAssignMap assignMap) {
        ServerTransportConfig transportConfig = new ServerTransportConfig()
            .setPort(assignMap.getPort())
            .setIdleSeconds(600);
        return sentinelApiClient.modifyClusterServerTransportConfig(app, homePage, transportConfig)
            .thenCompose(v -> applyServerFlowConfigChange(app, homePage, assignMap))
            .thenCompose(v -> applyServerNamespaceSetConfig(app, homePage, assignMap));
    }

    private CompletableFuture<Void> applyServerFlowConfigChange(String app, String homePage,
                                                                ClusterAppAssignMap assignMap) {
        Double maxAllowedQps = assignMap.getMaxAllowedQps();
        if (maxAllowedQps == null || maxAllowedQps <= 0 || maxAllowedQps > 20_0000) {
            return CompletableFuture.completedFuture(null);
        }
        return sentinelApiClient.modifyClusterServerFlowConfig(app, homePage,
            new ServerFlowConfig().setMaxAllowedQps(maxAllowedQps));
    }

    private CompletableFuture<Void> applyServerNamespaceSetConfig(String app, String homePage,
                                                                  ClusterAppAssignMap assignMap) {
        Set<String> namespaceSet = assignMap.getNamespaceSet();
        if (namespaceSet == null || namespaceSet.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return sentinelApiClient.modifyClusterServerNamespaceSet(app, homePage, namespaceSet);
    }

    private CompletableFuture<Void> modifyMode(String homePage, int mode) {
        return sentinelApiClient.modifyClusterMode(homePage, mode);
    }

    private int parsePort(ClusterAppAssignMap assignMap) {
        return MachineUtils.parseCommandPort(assignMap.getMachineId())
            .orElse(ServerTransportConfig.DEFAULT_PORT);
    }

}
