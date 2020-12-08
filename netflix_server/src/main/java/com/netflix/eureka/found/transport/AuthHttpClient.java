package com.netflix.eureka.found.transport;

import javax.ws.rs.core.Response;

import com.netflix.eureka.found.model.Cluster;
import com.netflix.eureka.found.model.Namespace;

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
	Response getClient(String namespaceId, String svrid);
	/**
	 * Set Cluster client info
	 * @param cluster
	 * @return
	 */
	Response setClient(Cluster cluster);
}
