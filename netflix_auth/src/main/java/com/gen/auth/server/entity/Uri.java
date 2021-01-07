package com.gen.auth.server.entity;

import java.util.Date;

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
	private Integer strategy;
	private String basepath;
	private String method;
	private String summary;
	private Integer status;
	private Date createtime;
	private Date updatetime;
}
