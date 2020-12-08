package com.gen.auth.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName(value = "auth_uri")
public class Uri {
	@TableId(type = IdType.ASSIGN_ID)
    private String id;
	
	private String svrid;
	private String basepath;
	private String method;
	private String summary;
	private String description;
}
