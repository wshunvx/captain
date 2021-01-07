package com.netflix.eureka.found.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.netflix.eureka.Version;
import com.netflix.eureka.found.model.Configinfo;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.resources.CurrentRequestVersion;

@Path("/{version}/config")
@Produces({"application/xml", "application/json"})
public class ConfigurationResource {
	
	@GET
    public Restresult<List<Configinfo>> getList() {
		Configinfo.Builder builder = Configinfo.Builder.newBuilder();
		builder.withBase("1000100110200002", "gen-demo");
		List<Configinfo> list = new ArrayList<>();
		list.add(builder.build());
		return Restresult.ofSuccess(list);
    }
	
	@PUT
    public Response putConfig(@QueryParam("dataId") String dataId,
    		@QueryParam("group") String group, @QueryParam("namespaceId") String namespaceId) {
		return Response.ok().build();
    }
	
	@DELETE
    public Response delConfig(@QueryParam("dataId") String dataId,
    		@QueryParam("group") String group, @QueryParam("namespaceId") String namespaceId) {
		return Response.ok().build();
    }
	
	@Path("{appId}")
    public ConfigurationAppResource getConfigurationMenuResource(
            @PathParam("version") String version,
            @PathParam("appId") String appId) {
        CurrentRequestVersion.set(Version.toEnum(version));
        try {
            return new ConfigurationAppResource(appId);
        } finally {
            CurrentRequestVersion.remove();
        }
    }
}
