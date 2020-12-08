package com.gen.auth.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName(value = "auth_type")
public class Svrtype {
	@TableId(type = IdType.INPUT)
    private String id;

    private String app;
    
    private String name;

    private Integer status;
    
}