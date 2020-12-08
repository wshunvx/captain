package com.netflix.eureka.found.resources.actuator.cluster;

import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.netflix.eureka.found.model.Namespace;
import com.netflix.eureka.found.transport.AuthHttpClient;

@Produces({"application/xml", "application/json"})
public class InstanceAuthResource {

	private AuthHttpClient httpClient;
	
    public InstanceAuthResource(AuthHttpClient httpClient) {
    	this.httpClient = httpClient;
    }
    
    @POST
    public Response apiAssignAllClusterServersOfApp(MultivaluedMap<String, String> queryParams) {
        return httpClient.setNamespace(inNamespace(queryParams));
    }
    
    private Namespace inNamespace(MultivaluedMap<String, String> queryParams) {
    	Namespace.Builder builder = Namespace.Builder.newBuilder();
    	builder.inGroup(queryParams.getFirst("groupname"));
    	builder.inId(queryParams.getFirst("id"));
    	builder.inZone(queryParams.getFirst("zone"));
    	builder.inHostname(queryParams.getFirst("hostname"));
    	builder.inIpaddr(queryParams.getFirst("ipaddr"));
    	String status = queryParams.getFirst("status");
    	if(status != null && status.length() > 0) {
    		builder.inStatus(Integer.valueOf(status));
    	}
    	return builder.build();
    }
}
