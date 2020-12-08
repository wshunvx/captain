/**
 * Create time 2019-08-04
 * Create by wangkai
 * Copyright © 南京捷鹰数码测绘有限公司 版权所有 All Rights Reserved.
 */

package com.gen.auth.server.mapper.area;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gen.auth.server.entity.area.Area;

import java.util.List;
import java.util.Set;

/**
 * 区域 Mapper 接口
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
public interface AreaMapper extends BaseMapper<Area> {


    /**
     * 树形结构
     *
     * @return
     */
    List<Area> tree(@Param("areaIds") Set<String> areaIds);


    /**
     * 树形结构
     *
     * @param rootId
     * @return
     */
    List<Area> treeByRootId(@Param("rootId") String rootId);
}
