/**
 * Create time 2019-08-04
 * Create by wangkai
 * Copyright © 南京捷鹰数码测绘有限公司 版权所有 All Rights Reserved.
 */

package com.gen.auth.server.mapper.menu;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gen.auth.server.entity.menu.Menu;
import com.gen.auth.server.entity.role.Role;

import java.util.List;

/**
 * 菜单 Mapper 接口
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 树形结构
     *
     * @return
     */
    List<Menu> tree(@Param("category") String[] category);

    /**
     * 权限配置菜单
     *
     * @param roles
     * @return
     */
    List<Menu> role(@Param("roles") List<Role> roles);

    /**
     * 按钮树形结构
     *
     * @param roles
     * @return
     */
    List<Menu> buttons(@Param("roles") List<Role> roles);
    
    /**
     * 按钮树形结构
     *
     * @param roles
     * @return
     */
    List<Menu> parents(@Param("roles") List<Role> roles);
    
    /**
     * 查询用户角色列表
     *
     * @param roleId
     * @return
     */
    List<Menu> permissions(@Param("userId") String userId);

}
