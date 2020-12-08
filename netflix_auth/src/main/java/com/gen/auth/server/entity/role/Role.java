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
 * 角色表 数据模型
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "role")
public class Role extends Entity {

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色权限字符串
     */
    private String sign;

    /**
     * 角色状态（数据字典维护）
     */
    private String status;

    /**
     * 显示顺序
     */
    private Integer sort;

}
