package com.gen.auth.server.biz;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gen.auth.server.entity.role.Role;
import com.gen.auth.server.mapper.role.RoleMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class RoleBiz extends BaseBiz<RoleMapper, Role> {
	public List<Role> roleByUser(String user_id) {
		return mapper.getRoleByUid(user_id);
	}
	
	public List<String> roleMenu(String sign) {
		return mapper.getRoleMenu(sign);
	}
	
}
