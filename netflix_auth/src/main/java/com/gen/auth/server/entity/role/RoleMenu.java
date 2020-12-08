/**
 * Create time 2019-08-04
 * Create by wangkai
 * Copyright © 南京捷鹰数码测绘有限公司 版权所有 All Rights Reserved.
 */
package com.gen.auth.server.entity.role;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gen.auth.server.entity.base.Entity;


/**
 * 角色-菜单关系 数据模型
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "role_menu")
public class RoleMenu extends Entity {

    /**
     * 菜单ID
     */
    private String menuId;

    /**
     * 角色ID
     */
    private String roleId;

}
