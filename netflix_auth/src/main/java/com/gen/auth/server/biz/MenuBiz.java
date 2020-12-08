package com.gen.auth.server.biz;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gen.auth.server.entity.menu.Menu;
import com.gen.auth.server.entity.role.Role;
import com.gen.auth.server.mapper.menu.MenuMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class MenuBiz extends BaseBiz<MenuMapper, Menu> {
	
	public List<String> code(String roleId) {
		List<Menu> list = mapper.permissions(roleId);
		if(list == null || list.isEmpty()) {
			return new ArrayList<String>();
		}
		return list.stream().map(role -> role.getCode()).collect(Collectors.toList());
	}
	
	public List<String> path(String roleId) {
		List<Menu> list = mapper.permissions(roleId);
		if(list == null || list.isEmpty()) {
			return new ArrayList<String>();
		}
		return list.stream().map(role -> role.getPath()).collect(Collectors.toList());
	}
	
	public List<Menu> buttons(List<Role> roles) {
		return mapper.buttons(roles);
	}
	
	public List<Menu> tree(String[] category) {
		return mapper.tree(category);
	}
	
	public List<Menu> roleMenu(List<Role> roles) {
		return mapper.role(roles);
	}
	
}
