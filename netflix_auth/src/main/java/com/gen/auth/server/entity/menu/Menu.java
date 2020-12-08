/**
 * Create time 2019-08-04
 * Create by wangkai
 * Copyright © 南京捷鹰数码测绘有限公司 版权所有 All Rights Reserved.
 */
package com.gen.auth.server.entity.menu;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gen.auth.server.entity.base.Entity;

/**
 * 菜单 数据模型
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "menu")
public class Menu extends Entity {

    /**
     * 父节点ID
     */
	@TableField(value = "parent_id")
    private String parentId;

    /**
     * 菜单编号
     */
    private String code;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 请求地址
     */
    private String path;

    /**
     * 菜单资源
     */
    private String source;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 菜单类型(0:导航1:菜单2:按钮)
     */
    private Integer category;


    /**
     * 是否打开新页面
     */
    @TableField(value="is_open")
    private String isOpen;

}
