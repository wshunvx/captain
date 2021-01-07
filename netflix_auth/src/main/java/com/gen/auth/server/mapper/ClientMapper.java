package com.gen.auth.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gen.auth.server.entity.Client;
import com.netflix.eureka.bean.ZClient;

public interface ClientMapper extends BaseMapper<Client> {
	
	ZClient findByInstanceId(String instanceId);
	
	int updateTs(Client client);
}