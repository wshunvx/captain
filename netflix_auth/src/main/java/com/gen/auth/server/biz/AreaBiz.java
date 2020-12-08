package com.gen.auth.server.biz;

import com.gen.auth.server.entity.area.Area;
import com.gen.auth.server.mapper.area.AreaMapper;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class AreaBiz extends BaseBiz<AreaMapper, Area> {
	
	public List<Area> tree(Set<String> areaIds) {
		return mapper.tree(areaIds);
	}


    public List<Area> treeByRootId(String rootId) {
    	return mapper.treeByRootId(rootId);
    }


	
}
