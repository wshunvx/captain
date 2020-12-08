package com.gen.auth.server.biz;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gen.auth.server.entity.menu.MenuUri;
import com.gen.auth.server.mapper.menu.MenuUriMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class MenuUriBiz extends BaseBiz<MenuUriMapper, MenuUri> {
	
	
}
