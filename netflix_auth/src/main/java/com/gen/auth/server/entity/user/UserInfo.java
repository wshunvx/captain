/**
 * Create time 2019-08-04
 * Create by wangkai
 * Copyright © 南京捷鹰数码测绘有限公司 版权所有 All Rights Reserved.
 */
package com.gen.auth.server.entity.user;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gen.auth.server.entity.base.Entity;


/**
 * 用户 数据模型
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_info")
public class UserInfo extends Entity {

	/**
	 * 身份标唯一识码
	 */
	private String identity;
    /**
     * 登录名
     */
    private String loginName;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 密码加密盐
     */
    private String salt;

    /**
     * 状态 数据字典维护
     */
    private Integer status;

    /**
     * 姓名
     */
    private String name;

    /**
     * 作业区
     */
    private String areaId;

    /**
     * 工号
     */
    private String jobNo;

    /**
     * 岗位
     */
    private String job;


    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像路径
     */
    private String photo;

    /**
     * 最后登陆IP
     */
    private String last_login_ip;

    /**
     * 最后登陆时间
     */
    private Date last_login_time;

}
