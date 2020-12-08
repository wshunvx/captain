package com.netflix.eureka.found.resources.actuator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import static com.netflix.eureka.command.CommandConstants.APP_TYPE_GATEWAY;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.netflix.eureka.dashboard.datasource.entity.MachineEntity;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/host")
@Produces({"application/xml", "application/json"})
public class HostResource {

    private PeerAwareInstanceRegistry instanceRegistry;

    @Inject
    HostResource(SentinelServerContext serverContext) {
        this.instanceRegistry = serverContext.getInstanceRegistry();
    }

    public HostResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }
    
    @GET
    @Path("names")
    public Restresult<Set<String>> queryApps() {
    	Set<String> names = new HashSet<String>();
    	Applications apps = instanceRegistry.getApplications();
		for(Application app: apps.getRegisteredApplications()) {
			names.add(app.getName());
		}
		return new Restresult<>(names);
        
    }
    
    @GET
    @Path("/machines")
    public Restresult<Map<String, Collection<MachineEntity>>> queryAppInfos(
    		@QueryParam("namespaceId") String namespaceId,
    		@Nullable @QueryParam("svrid") String svrid) {
    	Multimap<String, MachineEntity> machines = HashMultimap.create();
    	Applications apps = instanceRegistry.getApplications();
    	for(Application app: apps.getRegisteredApplications()) {
    		List<InstanceInfo> instanceInfos = app.getInstancesAsIsFromEureka();
    		for(InstanceInfo info: instanceInfos) {
    			String instanceId = info.getInstanceId();
    			if(instanceId.length() != 16) {
            		continue;
            	}
    			String type = instanceId.substring(8, 11);
    			if(APP_TYPE_GATEWAY.equals(type)) {
    				continue;
    			}
    			if((svrid == null || "".equals(svrid))) {
    				type = svrid = "";
            	}
    			if(namespaceId.equals(instanceId.substring(0, 5)) && type.equals(svrid)) {
    				machines.put(app.getName(), toMachineEntity(info));
    			}
    			
    		}
    	}
        return new Restresult<>(machines.asMap());
    }
    
    @GET
    @Path("/gateway")
    public Restresult<Map<String, Collection<MachineEntity>>> queryGateway(
    		@QueryParam("namespaceId") String namespaceId) {
    	Multimap<String, MachineEntity> machines = HashMultimap.create();
    	Applications apps = instanceRegistry.getApplications();
    	for(Application app: apps.getRegisteredApplications()) {
    		List<InstanceInfo> instanceInfos = app.getInstancesAsIsFromEureka();
    		for(InstanceInfo info: instanceInfos) {
    			String instanceId = info.getInstanceId();
    			if(instanceId.length() != 16) {
            		continue;
            	}
    			if(namespaceId.equals(instanceId.substring(0, 5)) && 
    					APP_TYPE_GATEWAY.equals(instanceId.substring(8, 11))) {
    				machines.put(app.getName(), toMachineEntity(info));
    			}
    			
    		}
    	}
        return new Restresult<>(machines.asMap());
    }

    public MachineEntity toMachineEntity(InstanceInfo instanceInfo) {
    	MachineEntity machineEntity = new MachineEntity();
    	machineEntity.setInstanceId(instanceInfo.getId());
    	machineEntity.setApp(instanceInfo.getAppName());
    	machineEntity.setIp(instanceInfo.getIPAddr());
    	machineEntity.setPort(instanceInfo.getPort());
    	machineEntity.setStatus(instanceInfo.getStatus().name());
    	machineEntity.setHostname(instanceInfo.getHostName());
    	machineEntity.setTimestamp(instanceInfo.getLastDirtyTimestamp());
    	return machineEntity;
    }
}
