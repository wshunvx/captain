package com.gen.auth.server.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName(value = "auth_ursa")
public class Ursa {
	@TableId(type = IdType.ASSIGN_ID)
    private String id;
	
	private String name;
	private String seeded;
	private String publicKey;
	private String privateKey;
	private Date expired;
	private Integer status;
	private Date createtime;
	private Date updatetime;
}
