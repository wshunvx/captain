package com.gen.auth.server.biz;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gen.auth.server.entity.user.UserRole;
import com.gen.auth.server.mapper.user.UserRoleMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserRoleBiz extends BaseBiz<UserRoleMapper, UserRole> {
	
	public List<String> signByUid(String user_id) {
		return mapper.getSignByUid(user_id);
	}
	
	public List<UserRole> roleByUser(String user_id) {
		return mapper.getRoleByUid(user_id);
	}
	
	public UserRole roleByRole(String user_id, String role_id) {
		return mapper.getRoleByRid(user_id, role_id);
	}
	
}
