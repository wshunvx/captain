/**
 * Create time 2019-08-04
 * Create by wangkai
 * Copyright © 南京捷鹰数码测绘有限公司 版权所有 All Rights Reserved.
 */

package com.gen.auth.server.mapper.role;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gen.auth.server.entity.role.RoleMenu;

/**
 * 角色-菜单关系 Mapper 接口
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
	List<RoleMenu> getRoleByRid(@Param("roleIds") List<String> roleIds);
	
	int updateFlag(RoleMenu roleMenu);
}
