package com.gen.auth.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gen.auth.server.biz.ClientBiz;
import com.gen.auth.server.biz.InstanceBiz;
import com.gen.auth.server.biz.SvrtypeBiz;
import com.gen.auth.server.entity.Client;
import com.gen.auth.server.entity.Instance;
import com.gen.auth.server.entity.Svrtype;
import com.netflix.eureka.bean.ZClient;
import com.netflix.eureka.command.CommandResponse;

@Service
public class ClientService {
    @Autowired
    private ClientBiz clientBiz;
    @Autowired
    private SvrtypeBiz svrtypeBiz;
    @Autowired
    private InstanceBiz instanceBiz;
    
    public CommandResponse<ZClient> getClient(String instanceId) {
    	ZClient client = clientBiz.findByInstanceId(instanceId);
		if(client == null) {
			return CommandResponse.ofFailure("InstanceId wrong information");
		}
		
		return CommandResponse.ofSuccess(client);
	}
    
	public CommandResponse<List<Client>> getClientAll(Map<String, Object> query) {
		QueryWrapper<Client> example = new QueryWrapper<>();
		if (query.entrySet().size() > 0) {
			for (Map.Entry<String, Object> entry : query.entrySet()) {
				example.eq(entry.getKey(), entry.getValue());
			}
		}
		
		List<Client> list = clientBiz.list(example);
		if(list == null) {
			list = new ArrayList<Client>();
		}
		
		return CommandResponse.ofSuccess(list);
	}
	
	public CommandResponse<Client> setClientAll(Client client) {
		if(client == null || (StringUtils.isEmpty(client.getId())
				|| StringUtils.isEmpty(client.getInstanceId()))) {
			return CommandResponse.ofFailure("(Id | InstanceId) value null");
		}
		
		if(client.getId().length() > 5 || (StringUtils.isEmpty(client.getSvrid()) 
				|| client.getSvrid().length() > 3)) {
			return CommandResponse.ofFailure("(Id | InstanceId) leng out of range");
		}
		
		Instance instance = instanceBiz.getById(client.getInstanceId());
		if(instance == null) {
			return CommandResponse.ofFailure("InstanceId wrong information");
		}
		
		StringBuffer clientId = new StringBuffer(instance.getId());
		String id = String.format("%05d", Integer.valueOf(client.getId()));
		String zone = String.format("%03d", Integer.valueOf(instance.getZone()));
		String svrid = String.format("%03d", Integer.valueOf(client.getSvrid()));
		clientId.append(zone).append(svrid).append(id);
		if(clientId.length() != 16) {
			return CommandResponse.ofFailure("ClientId leng out of range");
		}
		
		Client updateClient = clientBiz.getById(clientId.toString());
		if(updateClient == null) {
			client.setId(clientId.toString());
			client.setStatus(1);
			clientBiz.save(client);
		} else {
			updateClient.setSvrid(client.getSvrid());
			updateClient.setName(client.getName());
			updateClient.setSecret(client.getSecret());
			updateClient.setPort(client.getPort());
			clientBiz.updateById(updateClient);
		}
		
		return CommandResponse.ofSuccess(client);
	}

	public CommandResponse<List<Svrtype>> getSvrtype() {
		List<Svrtype> list = svrtypeBiz.list(new Svrtype());
		if(list == null) {
			list = new ArrayList<Svrtype>();
		}
		return CommandResponse.ofSuccess(list);
	}
	
	public CommandResponse<List<Instance>> getInstance() {
		List<Instance> list = instanceBiz.list(new Instance());
		if(list == null) {
			list = new ArrayList<Instance>();
		}
		return CommandResponse.ofSuccess(list);
	}
	
	public CommandResponse<Instance> setInstance(Instance instance) {
		if(instance == null || (StringUtils.isEmpty(instance.getId())
				|| StringUtils.isEmpty(instance.getZone()))) {
			return CommandResponse.ofFailure("(Id | Zone) value null");
		}
		
		if(instance.getId().length() != 5 || instance.getZone().length() > 3) {
			return CommandResponse.ofFailure("(Id | Zone) leng out of range");
		}
		
		Instance updateInstance = instanceBiz.getById(instance.getId());
		if(updateInstance == null) {
			instanceBiz.save(instance);
		} else {
			instanceBiz.updateById(instance);
		}
		
		return CommandResponse.ofSuccess(instance);
	}

	public Client getClient(String clientId, String secret) {
        Client client = clientBiz.getById(clientId);
        if(client == null || !client.getSecret().equals(secret)){
        	return null;
        }
        return client;
    }
}
