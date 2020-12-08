/**
 * Create time 2019-08-04
 * Create by wangkai
 * Copyright © 南京捷鹰数码测绘有限公司 版权所有 All Rights Reserved.
 */
package com.gen.auth.server.entity.user;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gen.auth.server.entity.base.Entity;


/**
 * 用户--角色 关系 数据模型
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_role")
public class UserRole extends Entity  {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 角色ID
     */
    private String roleId;

}
