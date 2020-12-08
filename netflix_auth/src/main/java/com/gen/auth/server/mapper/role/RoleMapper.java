package com.gen.auth.server.mapper.role;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gen.auth.server.entity.role.Role;

/**
 * 角色 Mapper 接口
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
public interface RoleMapper extends BaseMapper<Role> {
	List<Role> getRoleByUid(@Param("userId") String userId);
	List<String> getRoleMenu(@Param("sign") String sign);
}
