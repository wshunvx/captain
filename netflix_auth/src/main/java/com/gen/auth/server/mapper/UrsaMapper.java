package com.gen.auth.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gen.auth.server.entity.Ursa;

public interface UrsaMapper extends BaseMapper<Ursa> {
	int updateStatus(String id, Integer status);
}