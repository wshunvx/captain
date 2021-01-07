package com.netflix.eureka.found.transport;

import java.util.Date;

import javax.ws.rs.core.Response;

import com.netflix.eureka.bean.ZClient;
import com.netflix.eureka.found.model.Cluster;
import com.netflix.eureka.found.model.Namespace;
import com.netflix.eureka.found.model.Restresult;

public interface AuthHttpClient {
	/**
	 * Cluster list
	 * @return
	 */
	Response getNamespace();
	/**
	 * Service type
	 * @param namespace
	 * @return
	 */
	Response getSvrtype();
	/**
	 * Set Cluster info
	 * @return
	 */
	Response setNamespace(Namespace namespace);
	/**
	 * Cluster client list
	 * @param instanceId
	 * @param svrid
	 * @return
	 */
	Restresult<ZClient> getClient(String instanceId);
	Response getClient(String namespaceId, String svrid);
	/**
	 * Set Cluster client info
	 * @param cluster
	 * @return
	 */
	Response setClient(Cluster cluster);
	
	Response getRsaUris();
	
	Response setRsaUris(String id, String summary, String svrid, String basepath, String strategy, String method);
	
	Response delRsaUris(String id);
	
	Response getRsaFirst();
	
	Response getRsaReset();
	
	Response setUserRsa(String id, String name, String seeded, Date expired);
}
