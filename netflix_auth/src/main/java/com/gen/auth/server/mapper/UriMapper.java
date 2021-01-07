package com.gen.auth.server.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gen.auth.server.entity.Uri;

public interface UriMapper extends BaseMapper<Uri> {
	List<Uri> queryUriInSvrid();
	
	int updateStatus(String id, Integer status);
}