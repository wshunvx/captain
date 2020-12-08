package com.gen.auth.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName(value = "auth_client")
public class Client {
	@TableId(type = IdType.INPUT)
    private String id;

	@TableField(value = "instance_id")
    private String instanceId;

    private String svrid;
    
    private String secret;

    private String name;

    private Integer port;
    private Integer locked = 0;
    
    /**
     * 1.UP, 2.DOWN, 3.STARTING, 4.OUT_OF_SERVICE, 0.UNKNOWN;
     */
    private Integer status = 0;
    
    private String ts;
    
    public Client() {}
    
    public Client(String instanceId) {
    	this.instanceId = instanceId;
    }
    
    public boolean hasOnline() {
    	if(status == 0 || status == 2) {
    		return false;
    	}
    	return true;
    }
}