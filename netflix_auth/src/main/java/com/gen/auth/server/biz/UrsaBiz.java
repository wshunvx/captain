package com.gen.auth.server.biz;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gen.auth.server.entity.Ursa;
import com.gen.auth.server.mapper.UrsaMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class UrsaBiz extends BaseBiz<UrsaMapper, Ursa> {
	public int update(String id, int status) {
		return mapper.updateStatus(id, status);
	}
}
