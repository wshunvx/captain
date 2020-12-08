package com.gen.auth.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gen.auth.server.entity.Client;

public interface ClientMapper extends BaseMapper<Client> {
	int updateTs(Client client);
}