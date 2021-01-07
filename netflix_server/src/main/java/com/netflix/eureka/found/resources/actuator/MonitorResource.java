package com.netflix.eureka.found.resources.actuator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.alibaba.csp.sentinel.util.StringUtil;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.command.vo.NodeVo;
import com.netflix.eureka.dashboard.client.HttpapiClient;
import com.netflix.eureka.dashboard.domain.ResourceTreeNode;
import com.netflix.eureka.dashboard.domain.vo.ResourceVo;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.sentinel.ServerContext;
import com.netflix.eureka.found.sentinel.ServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/monitor")
@Produces({"application/xml", "application/json"})
public class MonitorResource {

    private HttpapiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    
    @Inject
    MonitorResource(ServerContext serverContext) {
        this.sentinelApiClient = serverContext.getHttpapiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
    }

    public MonitorResource() {
        this(ServerContextHolder.getSecurity().getServerContext());
    }

    @GET
    @Path("jsonTree")
    public Restresult<List<ResourceVo>> machineJsonTree(@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, @Nullable @QueryParam("type") String type,
    		@Nullable @QueryParam("searchKey") String searchKey) {
        InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
    	if(instanceInfo != null) {
    		List<NodeVo> nodeVos = sentinelApiClient.fetchResourceOfMachine(instanceInfo.getHomePageUrl(), type);
            if (nodeVos == null) {
                return Restresult.ofSuccess(new ArrayList<ResourceVo>());
            }
            ResourceTreeNode treeNode = ResourceTreeNode.fromNodeVoList(nodeVos);
            treeNode.searchIgnoreCase(searchKey);
            return Restresult.ofSuccess(ResourceVo.fromResourceTreeNode(treeNode));
    	}
        return new Restresult<List<ResourceVo>>();
    }
    
    @GET
    @Path("clusterNode")
    public Restresult<List<ResourceVo>> machineClusterNode(@QueryParam("app") String app, 
    		@QueryParam("instanceId") String instanceId, @Nullable @QueryParam("type") String type,
    		@Nullable @QueryParam("searchKey") String searchKey) {
        InstanceInfo instanceInfo = instanceRegistry.getInstanceByAppAndId(app, instanceId);
    	if(instanceInfo != null) {
    		List<NodeVo> nodeVos = sentinelApiClient.fetchClusterNodeOfMachine(instanceInfo.getHomePageUrl(), true);
            if (nodeVos == null) {
                return Restresult.ofSuccess(new ArrayList<ResourceVo>());
            }
            if (StringUtil.isNotEmpty(searchKey)) {
                nodeVos = nodeVos.stream().filter(node -> node.getResource()
                    .toLowerCase().contains(searchKey.toLowerCase()))
                    .collect(Collectors.toList());
            }
            return Restresult.ofSuccess(ResourceVo.fromNodeVoList(nodeVos));
    	}
        return new Restresult<List<ResourceVo>>();
    }
}
