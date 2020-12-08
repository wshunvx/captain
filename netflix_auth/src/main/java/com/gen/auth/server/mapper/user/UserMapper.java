/**
 * Create time 2019-08-04
 * Create by wangkai
 * Copyright © 南京捷鹰数码测绘有限公司 版权所有 All Rights Reserved.
 */

package com.gen.auth.server.mapper.user;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gen.auth.server.entity.area.Area;
import com.gen.auth.server.entity.user.UserInfo;

import java.util.Date;
import java.util.List;

/**
 * 用户 Mapper 接口
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
public interface UserMapper extends BaseMapper<UserInfo> {

    /**
     * 根据角色查询用户
     *
     * @param role
     * @return
     */
    List<UserInfo> getUserByRole(@Param("role") List<String> role);

    /**
     * 查询用户所在组
     * @param userId
     * @return
     */
    Area getUserByArea(@Param("userId") String userId);
    
    /**
     * 更新用户登录信息
     * @param user
     * @return
     */
    int updateLoginInfo(@Param("id") String id, @Param("identity") String identity, @Param("last_login_time") Date last_login_time);
}
