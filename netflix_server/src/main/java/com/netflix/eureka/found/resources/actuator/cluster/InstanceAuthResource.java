package com.netflix.eureka.found.resources.actuator.cluster;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.springframework.util.StringUtils;

import com.netflix.eureka.bean.ZClient;
import com.netflix.eureka.found.model.Namespace;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.registry.ServiceGenerator;
import com.netflix.eureka.found.transport.AuthHttpClient;

@Produces({"application/xml", "application/json"})
public class InstanceAuthResource {

	private AuthHttpClient httpClient;
	private ServiceGenerator svrGenerator;
	
    public InstanceAuthResource(ServiceGenerator svrGenerator, AuthHttpClient httpClient) {
    	this.svrGenerator = svrGenerator;
    	this.httpClient = httpClient;
    }
    
    @GET
    public Restresult<String> getClientById(@QueryParam("instanceId") String instanceId) {
    	Restresult<ZClient> result = httpClient.getClient(instanceId);
    	if(result == null || result.getCode() != 1000) {
    		return Restresult.ofFailure(1001, "result null");
    	}
    	
    	ZClient client = result.getData();
    	if(client == null || (StringUtils.isEmpty(client.getId()) || StringUtils.isEmpty(client.getHostname()))) {
    		return Restresult.ofFailure(1001, "data null");
    	}
    	StringBuffer yaml = new StringBuffer("{\"instance\": {\"hostName\": \"");
    	yaml.append(client.getHostname()).append("\", \"instanceId\": \"");
    	yaml.append(client.getId()).append("\", \"metadataMap\": {\"zone\": \"");
    	yaml.append(client.getZone()).append("\", \"secret\": \"");
    	yaml.append(svrGenerator.sign(client.toString())).append("\"},\"appGroupName\": \"");
    	yaml.append(client.getGroupname()).append("\"}, \"client\": {\"zone\": \"1\", \"preferSameZoneEureka\": true, \"serviceUrl\": {\"defaultZone\": \"http://user:password@localhost:8761/eureka\"}}}");
    	return Restresult.ofSuccess(yaml.toString());
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
