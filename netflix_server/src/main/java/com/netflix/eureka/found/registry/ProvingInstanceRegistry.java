package com.netflix.eureka.found.registry;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.server.InstanceRegistry;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.bean.ZClient;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.eureka.resources.ServerCodecs;

public class ProvingInstanceRegistry extends InstanceRegistry implements ApplicationContextAware {
	private static Logger log = LoggerFactory.getLogger(ProvingInstanceRegistry.class);
	
	private final ServiceGenerator generator;
	
	public ProvingInstanceRegistry(ServiceGenerator generator, EurekaServerConfig serverConfig, EurekaClientConfig clientConfig,
			ServerCodecs serverCodecs, EurekaClient eurekaClient, int expectedNumberOfClientsSendingRenews,
			int defaultOpenForTrafficCount) {
		super(serverConfig, clientConfig, serverCodecs, eurekaClient, expectedNumberOfClientsSendingRenews,
				defaultOpenForTrafficCount);
		this.generator = generator;
	}

	@Override
	public void register(InstanceInfo info, int leaseDuration, boolean isReplication) {
		if(verifyToInstanceIdAndHostName(info)) {
			super.register(info, leaseDuration, isReplication);
		} else {
			log.warn(info.toString());
		}
	}

	@Override
	public void register(final InstanceInfo info, final boolean isReplication) {
		if(verifyToInstanceIdAndHostName(info)) {
			super.register(info, isReplication);
		} else {
			log.warn(info.toString());
		}
	}
	
	protected boolean verifyToInstanceIdAndHostName(final InstanceInfo info) {
		Map<String, String> metadata = info.getMetadata();
		String secret = metadata.get("secret");
		if(StringUtils.isEmpty(secret)) {
			return false;
		}
		
		byte[] decrypt = generator.verify(secret);
		if(decrypt == null || decrypt.length == 0) {
			return false;
		}
		
		// Must be verified (instanceId and hostName)
		ZClient obj = JSONFormatter.fromJSON(new String(decrypt), ZClient.class);
		if(obj == null || (StringUtils.isEmpty(obj.getId()) || StringUtils.isEmpty(obj.getHostname()))) {
			return false;
		}
		
		return toZClient(info).equals(obj);
	}
	
	protected ZClient toZClient(InstanceInfo info) {
		ZClient client = new ZClient();
		client.setId(info.getInstanceId());
		client.setHostname(info.getHostName());
		client.setIpaddr(info.getIPAddr());
		client.setGroupname(info.getAppGroupName());
		client.setPort(info.getPort());
		Map<String, String> metadata = info.getMetadata();
		if(metadata != null) {
			client.setZone(metadata.get("zone"));
		}
		return client;
	}

}
