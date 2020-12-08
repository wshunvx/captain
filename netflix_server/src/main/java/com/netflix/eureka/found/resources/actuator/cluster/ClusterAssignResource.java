package com.netflix.eureka.found.resources.actuator.cluster;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.alibaba.csp.sentinel.util.StringUtil;

import com.netflix.eureka.dashboard.domain.cluster.ClusterAppAssignResultVO;
import com.netflix.eureka.dashboard.domain.cluster.ClusterAppFullAssignRequest;
import com.netflix.eureka.dashboard.domain.cluster.ClusterAppSingleServerAssignRequest;
import com.netflix.eureka.dashboard.service.ClusterService;
import com.netflix.eureka.found.model.Restresult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces({"application/xml", "application/json"})
public class ClusterAssignResource {

    private final Logger logger = LoggerFactory.getLogger(ClusterAssignResource.class);

    private ClusterService clusterService;

    public ClusterAssignResource(ClusterService clusterService) {
    	this.clusterService = clusterService;
    }
    
    @POST
    @Path("all_server")
    public Restresult<ClusterAppAssignResultVO> apiAssignAllClusterServersOfApp(@QueryParam("app") String app, 
    		ClusterAppFullAssignRequest assignRequest) {
        if (StringUtil.isEmpty(app)) {
            return new Restresult<>(-1, "app cannot be null or empty");
        }
        if (assignRequest == null || assignRequest.getClusterMap() == null
            || assignRequest.getRemainingList() == null) {
            return new Restresult<>(-1, "bad request body");
        }
        try {
            return new Restresult<>(clusterService.applyAssignToApp(app, assignRequest.getClusterMap(),
                assignRequest.getRemainingList()));
        } catch (Throwable throwable) {
            logger.error("Error when assigning full cluster servers for app: " + app, throwable);
            return new Restresult<>(-1, throwable.getMessage());
        }
    }

    @POST
    @Path("single_server")
    public Restresult<ClusterAppAssignResultVO> apiAssignSingleClusterServersOfApp(@QueryParam("app") String app, 
    		ClusterAppSingleServerAssignRequest assignRequest) {
        if (StringUtil.isEmpty(app)) {
            return new Restresult<>(-1, "app cannot be null or empty");
        }
        if (assignRequest == null || assignRequest.getClusterMap() == null) {
            return new Restresult<>(-1, "bad request body");
        }
        try {
            return new Restresult<>(clusterService.applyAssignToApp(app, Collections.singletonList(assignRequest.getClusterMap()),
                assignRequest.getRemainingList()));
        } catch (Throwable throwable) {
            logger.error("Error when assigning single cluster servers for app: " + app, throwable);
            return new Restresult<>(-1, throwable.getMessage());
        }
    }

    @POST
    @Path("unbind_server")
    public Restresult<ClusterAppAssignResultVO> apiUnbindClusterServersOfApp(
    		@QueryParam("app") String app, Set<String> machineIds) {
        if (StringUtil.isEmpty(app)) {
            return new Restresult<>(-1, "app cannot be null or empty");
        }
        if (machineIds == null || machineIds.isEmpty()) {
            return new Restresult<>(-1, "bad request body");
        }
        try {
            return new Restresult<>(clusterService.unbindClusterServers(app, machineIds));
        } catch (Throwable throwable) {
            logger.error("Error when unbinding cluster server {} for app <{}>", machineIds, app, throwable);
            return new Restresult<>(-1, throwable.getMessage());
        }
    }
}
