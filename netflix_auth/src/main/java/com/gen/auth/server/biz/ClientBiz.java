package com.gen.auth.server.biz;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gen.auth.server.entity.Client;
import com.gen.auth.server.mapper.ClientMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class ClientBiz extends BaseBiz<ClientMapper, Client> {
	public int updateTs(String id, String ts) {
		Client client = new Client();
		client.setId(id);
		client.setTs(ts);
		return mapper.updateTs(client);
	}
	
}