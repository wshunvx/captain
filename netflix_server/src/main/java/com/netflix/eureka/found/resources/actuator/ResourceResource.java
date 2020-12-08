package com.netflix.eureka.found.resources.actuator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.alibaba.csp.sentinel.util.StringUtil;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.command.vo.NodeVo;
import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.domain.ResourceTreeNode;
import com.netflix.eureka.dashboard.domain.vo.ResourceVo;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/resource")
@Produces({"application/xml", "application/json"})
public class ResourceResource {

    private SentinelApiClient sentinelApiClient;
    private PeerAwareInstanceRegistry instanceRegistry;
    
    @Inject
    ResourceResource(SentinelServerContext serverContext) {
        this.sentinelApiClient = serverContext.getSentinelApiClient();
        this.instanceRegistry = serverContext.getInstanceRegistry();
    }

    public ResourceResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }

    /**
     * Fetch real time statistics info of the machine.
     *
     * @param ip        ip to fetch
     * @param port      port of the ip
     * @param type      one of [root, default, cluster], 'root' means fetching from tree root node, 'default' means
     *                  fetching from tree default node, 'cluster' means fetching from cluster node.
     * @param searchKey key to search
     * @return node statistics info.
     */
    @GET
    public Restresult<List<ResourceVo>> machineResource(
    		@QueryParam("app") String app, @QueryParam("id") String id, 
    		@QueryParam("type") String type, @QueryParam("searchKey") String searchKey) {
        if (StringUtil.isEmpty(type)) {
            type = "default";
        }
        
        InstanceInfo instanceInfo = null;
    	Application application = instanceRegistry.getApplication(app);
    	if(application != null) {
    		instanceInfo = application.getByInstanceId(id);
    	}
    	
    	if(instanceInfo != null) {
    		if ("default".equalsIgnoreCase(type)) {
                List<NodeVo> nodeVos = sentinelApiClient.fetchResourceOfMachine(instanceInfo.getHomePageUrl(), type);
                if (nodeVos == null) {
                    return new Restresult<>(new ArrayList<ResourceVo>());
                }
                ResourceTreeNode treeNode = ResourceTreeNode.fromNodeVoList(nodeVos);
                treeNode.searchIgnoreCase(searchKey);
                return new Restresult<>(ResourceVo.fromResourceTreeNode(treeNode));
            } else {
                // Normal (cluster node).
                List<NodeVo> nodeVos = sentinelApiClient.fetchClusterNodeOfMachine(instanceInfo.getHomePageUrl(), true);
                if (nodeVos == null) {
                    return new Restresult<>(new ArrayList<ResourceVo>());
                }
                if (StringUtil.isNotEmpty(searchKey)) {
                    nodeVos = nodeVos.stream().filter(node -> node.getResource()
                        .toLowerCase().contains(searchKey.toLowerCase()))
                        .collect(Collectors.toList());
                }
                return new Restresult<>(ResourceVo.fromNodeVoList(nodeVos));
            }
    	}
        return new Restresult<List<ResourceVo>>();
    }
}
