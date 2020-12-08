package com.gen.auth.server.biz;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gen.auth.server.entity.role.RoleMenu;
import com.gen.auth.server.mapper.role.RoleMenuMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class RoleMenuBiz extends BaseBiz<RoleMenuMapper, RoleMenu> {
	public List<RoleMenu> roleByRole(List<String> roleIds) {
		return mapper.getRoleByRid(roleIds);
	}
	
}
