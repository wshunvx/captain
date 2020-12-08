package com.gen.auth.server.biz;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gen.auth.server.entity.Instance;
import com.gen.auth.server.mapper.InstanceMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class InstanceBiz extends BaseBiz<InstanceMapper, Instance> {

}
