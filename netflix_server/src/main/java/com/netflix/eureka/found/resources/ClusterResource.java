package com.netflix.eureka.found.resources;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.google.gson.JsonObject;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.dashboard.client.CommandNotFoundException;
import com.netflix.eureka.dashboard.domain.cluster.request.ClusterClientModifyRequest;
import com.netflix.eureka.dashboard.domain.cluster.request.ClusterModifyRequest;
import com.netflix.eureka.dashboard.domain.cluster.request.ClusterServerModifyRequest;
import com.netflix.eureka.dashboard.domain.cluster.state.AppClusterClientStateWrapVO;
import com.netflix.eureka.dashboard.domain.cluster.state.AppClusterServerStateWrapVO;
import com.netflix.eureka.dashboard.domain.cluster.state.ClusterUniversalStatePairVO;
import com.netflix.eureka.dashboard.domain.cluster.state.ClusterUniversalStateVO;
import com.netflix.eureka.dashboard.service.ClusterService;
import com.netflix.eureka.dashboard.util.ClusterEntityUtils;
import com.netflix.eureka.found.model.Cluster;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.resources.actuator.cluster.ClusterAssignResource;
import com.netflix.eureka.found.resources.actuator.cluster.InstanceAuthResource;
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.found.transport.AuthHttpClient;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/cluster")
@Produces({"application/xml", "application/json"})
public class ClusterResource {
	private final Logger logger = LoggerFactory.getLogger(ClusterResource.class);

	private AuthHttpClient httpClient;
	private ClusterService clusterService;
    private PeerAwareInstanceRegistry registry;
	
	@Inject
	ClusterResource(SentinelServerContext serverContext) {
        this.httpClient = serverContext.getAuthHttpClient();
        this.clusterService = serverContext.getService();
        this.registry = serverContext.getInstanceRegistry();
    }

    public ClusterResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }
    
    @GET
    public Response getContainers(
    		@QueryParam("instanceId") String instanceId,
    		@Nullable @QueryParam("svrid") String svrid) {
    	return httpClient.getClient(instanceId, svrid);
    }
    
    @POST
    public Response setContainers(MultivaluedMap<String, String> queryParams) {
    	return httpClient.setClient(inCluster(queryParams));
    }
    
    @Path("assign")
    public ClusterAssignResource getClusterAssign() {
    	return new ClusterAssignResource(clusterService);
    }
    
    @Path("instance")
    public InstanceAuthResource getClusterInstance() {
    	return new InstanceAuthResource(httpClient);
    }
    
    @POST
    @Path("modify_single")
    public Restresult<Boolean> apiModifyClusterConfig(String payload) {
        if (StringUtil.isBlank(payload)) {
            return new Restresult<>(-1, "empty request body");
        }
        try {
            JsonObject body = JSONFormatter.fromJSON(payload, JsonObject.class);
            if (body.has(KEY_MODE)) {
                int mode = body.get(KEY_MODE).getAsInt();
                switch (mode) {
                    case ClusterStateManager.CLUSTER_CLIENT:
                        ClusterClientModifyRequest data = JSONFormatter.fromJSON(payload, ClusterClientModifyRequest.class);
                        Restresult<Boolean> res = checkValidRequest(data);
                        if (res != null) {
                            return res;
                        }
                        clusterService.modifyClusterClientConfig(data).get();
                        return new Restresult<>(true);
                    case ClusterStateManager.CLUSTER_SERVER:
                        ClusterServerModifyRequest d = JSONFormatter.fromJSON(payload, ClusterServerModifyRequest.class);
                        Restresult<Boolean> r = checkValidRequest(d);
                        if (r != null) {
                            return r;
                        }
                        // TODO: bad design here, should refactor!
                        clusterService.modifyClusterServerConfig(d).get();
                        return new Restresult<>(true);
                    default:
                        return new Restresult<>(-1, "invalid mode");
                }
            }
            return new Restresult<>(-1, "invalid parameter");
        } catch (ExecutionException ex) {
            logger.error("Error when modifying cluster config", ex.getCause());
            return errorResponse(ex);
        } catch (Throwable ex) {
            logger.error("Error when modifying cluster config", ex);
            return new Restresult<>(-1, ex.getMessage());
        }
    }

    @GET
    @Path("state_single")
    public Restresult<ClusterUniversalStateVO> apiGetClusterState(@QueryParam("app") String app, @QueryParam("id") String id) {
        if (StringUtil.isEmpty(app)) {
            return new Restresult<>(-1, "app cannot be null or empty");
        }
        if (StringUtil.isEmpty(id)) {
            return new Restresult<>(-1, "id cannot be null or empty");
        }
        try {
        	InstanceInfo instanceInfo = null;
        	Application application = registry.getApplication(app);
        	if(application != null) {
        		instanceInfo = application.getByInstanceId(id);
        	}
        	
        	if(instanceInfo != null) {
        		return clusterService.getClusterUniversalState(app, instanceInfo.getHomePageUrl())
                        .thenApply(Restresult::new)
                        .get();
        	}
        	
            return new Restresult<ClusterUniversalStateVO>();
        } catch (ExecutionException ex) {
            logger.error("Error when fetching cluster state", ex.getCause());
            return errorResponse(ex);
        } catch (Throwable throwable) {
            logger.error("Error when fetching cluster state", throwable);
            return new Restresult<>(-1, throwable.getMessage());
        }
    }

    @GET
    @Path("server_state")
    public Restresult<List<AppClusterServerStateWrapVO>> apiGetClusterServerStateOfApp(@QueryParam("app") String app) {
        if (StringUtil.isEmpty(app)) {
            return new Restresult<>(-1, "app cannot be null or empty");
        }
        try {
            return clusterService.getClusterUniversalState(app)
                .thenApply(ClusterEntityUtils::wrapToAppClusterServerState)
                .thenApply(Restresult::new)
                .get();
        } catch (ExecutionException ex) {
            logger.error("Error when fetching cluster server state of app: " + app, ex.getCause());
            return errorResponse(ex);
        } catch (Throwable throwable) {
            logger.error("Error when fetching cluster server state of app: " + app, throwable);
            return new Restresult<>(-1, throwable.getMessage());
        }
    }

    @GET
    @Path("client_state")
    public Restresult<List<AppClusterClientStateWrapVO>> apiGetClusterClientStateOfApp(@QueryParam("app") String app) {
        if (StringUtil.isEmpty(app)) {
            return new Restresult<>(-1, "app cannot be null or empty");
        }
        try {
            return clusterService.getClusterUniversalState(app)
                .thenApply(ClusterEntityUtils::wrapToAppClusterClientState)
                .thenApply(Restresult::new)
                .get();
        } catch (ExecutionException ex) {
            logger.error("Error when fetching cluster token client state of app: " + app, ex.getCause());
            return errorResponse(ex);
        } catch (Throwable throwable) {
            logger.error("Error when fetching cluster token client state of app: " + app, throwable);
            return new Restresult<>(-1, throwable.getMessage());
        }
    }

    @GET
    @Path("state")
    public Restresult<List<ClusterUniversalStatePairVO>> apiGetClusterStateOfApp(@QueryParam("app") String app) {
        if (StringUtil.isEmpty(app)) {
            return new Restresult<>(-1, "app cannot be null or empty");
        }
        try {
            return clusterService.getClusterUniversalState(app)
                .thenApply(Restresult::new)
                .get();
        } catch (ExecutionException ex) {
            logger.error("Error when fetching cluster state of app: " + app, ex.getCause());
            return errorResponse(ex);
        } catch (Throwable throwable) {
            logger.error("Error when fetching cluster state of app: " + app, throwable);
            return new Restresult<>(-1, throwable.getMessage());
        }
    }

    private Cluster inCluster(MultivaluedMap<String, String> queryParams) {
    	Cluster.Builder builder = Cluster.Builder.newBuilder();
    	builder.inInstanceId(queryParams.getFirst("instanceId"));
    	builder.inId(queryParams.getFirst("id"));
    	builder.inSvrid(queryParams.getFirst("svrid"));
    	builder.inSecret(queryParams.getFirst("secret"));
    	builder.inName(queryParams.getFirst("name"));
    	String port = queryParams.getFirst("port");
    	if(port != null && port.length() > 0) {
    		builder.inPort(Integer.valueOf(port));
    	}
    	return builder.build();
    }
    
    private <T> Restresult<T> errorResponse(ExecutionException ex) {
        if (isNotSupported(ex.getCause())) {
            return unsupportedVersion();
        } else {
        	Throwable throwable = ex.getCause();
            return new Restresult<>(-1, throwable.getClass().getName() + ", " + throwable.getMessage());
        }
    }
    
    private boolean isNotSupported(Throwable ex) {
        return ex instanceof CommandNotFoundException;
    }

    private Restresult<Boolean> checkValidRequest(ClusterModifyRequest request) {
        if (StringUtil.isEmpty(request.getApp())) {
            return new Restresult<>(-1, "app cannot be empty");
        }
        if (StringUtil.isEmpty(request.getId())) {
            return new Restresult<>(-1, "id cannot be empty");
        }
        if (request.getMode() == null || request.getMode() < 0) {
            return new Restresult<>(-1, "invalid mode");
        }
        return null;
    }

    private <R> Restresult<R> unsupportedVersion() {
        return new Restresult<>(4041, "Sentinel client not supported for cluster flow control (unsupported version or dependency absent)");
    }

    private static final String KEY_MODE = "mode";
    
}
