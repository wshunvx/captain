package com.netflix.eureka.found.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.util.StringUtils;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.cluster.PeerEurekaNode;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.model.Serviceinfo;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/server")
@Produces({"application/xml", "application/json"})
public class ServerViewResource {
    private final PeerAwareInstanceRegistry registry;
	
	@Inject
	ServerViewResource(EurekaServerContext eurekaServer) {
        this.registry = eurekaServer.getRegistry();
    }
	
	public ServerViewResource() {
        this(EurekaServerContextHolder.getInstance().getServerContext());
    }
	
	@GET
    public Restresult<InstanceInfo> getAppInfo(
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId) {
		InstanceInfo instanceInfo = registry.getInstanceByAppAndId(app, instanceId);
		if (instanceInfo == null) {
            return new Restresult<InstanceInfo>();
        }
		
		return new Restresult<InstanceInfo>(instanceInfo);
	}
	
	@PUT
    public Response putContainers(@QueryParam("value") String newStatus,
    		@QueryParam("app") String app, @QueryParam("instanceId") String instanceId, 
    		@HeaderParam(PeerEurekaNode.HEADER_REPLICATION) String isReplication,
    		@QueryParam("lastDirtyTimestamp") String lastDirtyTimestamp) {
		try {
			if (registry.getInstanceByAppAndId(app, instanceId) == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
			
            boolean isSuccess = registry.statusUpdate(app, instanceId,
                    InstanceStatus.valueOf(newStatus), lastDirtyTimestamp,
                    "true".equals(isReplication));
            if (isSuccess) {
                return Response.ok().build();
            } else {
                return Response.serverError().build();
            }
        } catch (Throwable e) {
            return Response.serverError().build();
        }
    }
	
	@DELETE
    public Response delContainers(@QueryParam("app") String app, @QueryParam("instanceId") String instanceId, 
    			@HeaderParam(PeerEurekaNode.HEADER_REPLICATION) String isReplication,
    			@QueryParam("lastDirtyTimestamp") String lastDirtyTimestamp) {
		try {
			if (registry.getInstanceByAppAndId(app, instanceId) == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
			
            boolean isSuccess = registry.cancel(app, instanceId, "true".equals(isReplication));
            if (isSuccess) {
                return Response.ok().build();
            } else {
                return Response.serverError().build();
            }
        } catch (Throwable e) {
            return Response.serverError().build();
        }
    }
	
	@POST
    public Restresult<List<Serviceinfo>> getContainers(@QueryParam("namespaceId") String namespaceId,
    		@Nullable @QueryParam("serviceName") String serviceName, @Nullable @QueryParam("groupName") String groupName) {
        List<Serviceinfo> list = new ArrayList<>();
        Applications apps = registry.getApplications();
        for(Application app: apps.getRegisteredApplications()) {
        	Serviceinfo.Builder builder = Serviceinfo.Builder.newBuilder();
        	builder.withBase(app.getName());
        	
        	List<InstanceInfo> instanceInfos = app.getInstancesAsIsFromEureka();
        	int instanceCount = instanceInfos.size();
        	if(instanceCount > 0) {
        		int healthyInstanceCount = 0;
        		Set<String> clusterCount = new HashSet<>();
        		Set<String> appGroupName = new HashSet<>();
        		for(InstanceInfo info: instanceInfos) {
        			String instanceId = info.getInstanceId();
        			if(instanceId.length() != 16 || namespaceId.equals(instanceId.substring(5, 8))) {
                		continue;
                	}
        			
        			if(InstanceStatus.UP == info.getStatus()) {
        				healthyInstanceCount += 1;
        			}
        			
        			String ipAddr = info.getIPAddr();
        			if(StringUtils.isEmpty(ipAddr)) {
        				ipAddr = info.getHostName();
        			}
        			clusterCount.add(ipAddr);
        			appGroupName.add(info.getAppGroupName());
        		}
        		builder.inHealthyInstanceCount(healthyInstanceCount);
        		builder.inClusterCount(clusterCount.size());
        		builder.inGroupCount(appGroupName.size());
        	}
        	builder.inIpCount(instanceCount);
        	list.add(builder.build());
        }
		
		return new Restresult<List<Serviceinfo>>(list);
    }
}
