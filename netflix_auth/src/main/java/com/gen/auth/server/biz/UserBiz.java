package com.gen.auth.server.biz;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gen.auth.server.entity.area.Area;
import com.gen.auth.server.entity.user.UserInfo;
import com.gen.auth.server.mapper.user.UserMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserBiz extends BaseBiz<UserMapper, UserInfo> {

    public int updateUser(UserInfo entity) {
    	return mapper.updateById(entity);
    }

    /**
     * 根据用户名获取用户信息
     * @param username
     * @return
     */
    public UserInfo getUserByUsername(String username){
    	QueryWrapper<UserInfo> user = new QueryWrapper<>();
    	user.eq("login_name", username);
        return mapper.selectOne(user);
    }

    public Area getUserByArea(String userId) {
    	return mapper.getUserByArea(userId);
    }
    
    public List<UserInfo> getUserByRole(List<String> role) {
    	return mapper.getUserByRole(role);
    }
    
    public int updateLoginInfo(String userId, String identity) {
    	return mapper.updateLoginInfo(userId, identity, new Date());
    }
    
}
