package com.netflix.eureka.found.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

import com.netflix.eureka.found.model.Configinfo;
import com.netflix.eureka.found.model.Restresult;

@Produces({"application/xml", "application/json"})
public class ConfigurationAppResource {
	private final String appId;
	
	ConfigurationAppResource(String appId) {
		this.appId = appId;
    }
	
	@GET
    public Restresult<Configinfo> getConfigInfo() {
		Configinfo.Builder builder = Configinfo.Builder.newBuilder();
		builder.withBase(appId, "gen-demo");
		builder.inGroup("DEMO").inContent("# Test");
		return new Restresult<Configinfo>(builder.build());
	}
	
	@POST
	public Restresult<String> setConfigInfo() {
		return new Restresult<String>("Success");
	}
}
