package com.netflix.eureka.found.resources.actuator;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.dashboard.client.HttpapiClient;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.sentinel.ServerContext;
import com.netflix.eureka.found.sentinel.ServerContextHolder;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/resource")
@Produces({"application/xml", "application/json"})
public class ResourceResource {
	private final Logger logger = LoggerFactory.getLogger(ResourceResource.class);

    private HttpapiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    
    @Inject
    ResourceResource(ServerContext serverContext) {
        this.sentinelApiClient = serverContext.getHttpapiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
    }

    public ResourceResource() {
        this(ServerContextHolder.getSecurity().getServerContext());
    }

    @GET
    public Restresult<Object> machineResource(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
    	try {
    		InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
        	if(instanceInfo != null) {
        		JsonElement future = sentinelApiClient.fetchResourceHttpPath(instanceInfo.getHomePageUrl()).get();
                if (future != null) {
                	return Restresult.ofSuccess(JSONFormatter.fromJSON(future, Map.class));
                }
        	}
    	} catch (Throwable throwable) {
            logger.error("query gateway flow rules error:", throwable);
            return errorResponse(throwable);
        }
        return new Restresult<>();
    }
    
    private <T> Restresult<T> errorResponse(Throwable ex) {
        return Restresult.ofFailure(-1, ex.getClass().getName() + ", " + ex.getMessage());
    }
}
