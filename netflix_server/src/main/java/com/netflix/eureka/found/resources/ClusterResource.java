package com.netflix.eureka.found.resources;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.netflix.eureka.found.model.Cluster;
import com.netflix.eureka.found.registry.ServiceGenerator;
import com.netflix.eureka.found.resources.actuator.cluster.InstanceAuthResource;
import com.netflix.eureka.found.sentinel.ServerContext;
import com.netflix.eureka.found.sentinel.ServerContextHolder;
import com.netflix.eureka.found.transport.AuthHttpClient;

@Path("/{version}/cluster")
@Produces({"application/xml", "application/json"})
public class ClusterResource {

	private AuthHttpClient httpClient;
	private ServiceGenerator svrGenerator;
	
	@Inject
	ClusterResource(ServerContext serverContext) {
        this.httpClient = serverContext.getAuthHttpClient();
        this.svrGenerator = serverContext.getServiceGenerator();
    }

    public ClusterResource() {
        this(ServerContextHolder.getSecurity().getServerContext());
    }
    
    @GET
    public Response getContainers(
    		@QueryParam("namespaceId") String instanceId,
    		@Nullable @QueryParam("svrid") String svrid) {
    	return httpClient.getClient(instanceId, svrid);
    }
    
    @POST
    public Response setContainers(MultivaluedMap<String, String> queryParams) {
    	return httpClient.setClient(inCluster(queryParams));
    }
    
    @Path("instance")
    public InstanceAuthResource getClusterInstance() {
    	return new InstanceAuthResource(svrGenerator, httpClient);
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
    
}
