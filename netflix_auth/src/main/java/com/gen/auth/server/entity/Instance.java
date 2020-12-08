package com.gen.auth.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName(value = "auth_instance")
public class Instance {
	@TableId(type = IdType.ASSIGN_ID)
    private String id;
	
	private String zone;
	private String hostname;
	private String groupname;
	private String ipaddr;
	
	private Integer status;
}
