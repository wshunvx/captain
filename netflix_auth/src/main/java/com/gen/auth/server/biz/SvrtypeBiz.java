package com.gen.auth.server.biz;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gen.auth.server.entity.Svrtype;
import com.gen.auth.server.mapper.SvrtypeMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class SvrtypeBiz extends BaseBiz<SvrtypeMapper, Svrtype> {
	
}