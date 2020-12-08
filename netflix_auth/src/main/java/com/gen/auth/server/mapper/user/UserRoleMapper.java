/**
 * Create time 2019-08-04
 * Create by wangkai
 * Copyright © 南京捷鹰数码测绘有限公司 版权所有 All Rights Reserved.
 */

package com.gen.auth.server.mapper.user;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gen.auth.server.entity.user.UserRole;

/**
 * 用户--角色 关系表 Mapper 接口
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {
	UserRole getRoleByRid(@Param("userId") String userId, @Param("roleId") String roleId);
	
	/**
     * 查询用户角色
     *
     * @param userId
     * @return
     */
    List<String> getSignByUid(@Param("userId") String userId);
    
    List<UserRole> getRoleByUid(@Param("userId") String userId);
    
    int update(UserRole roleId);
    int updateFlag(UserRole roleId);
}
