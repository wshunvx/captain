package com.netflix.eureka.found.transport.rule;

import com.netflix.appinfo.InstanceInfo;

public interface AuthRule {
	public InstanceInfo choose();
}
