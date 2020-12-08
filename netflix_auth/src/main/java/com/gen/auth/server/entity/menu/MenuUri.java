package com.gen.auth.server.entity.menu;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName(value = "menu_uri")
public class MenuUri {
	@TableId(type = IdType.ASSIGN_ID)
    private String id;
	
	private String uriId;
	private String menuId;

}
