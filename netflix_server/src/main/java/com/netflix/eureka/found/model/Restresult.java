package com.netflix.eureka.found.model;

import com.netflix.discovery.provider.Serializer;

@Serializer
public class Restresult<R> {
	protected int code;
	protected R data;
    
	protected String desc;
	
	public Restresult() {}
    
	public Restresult(R result) {
		this(result, 1000, null);
    }
	
	public Restresult(int code, R result) {
		this(result, code, null);
    }
	
	public Restresult(int code, String desc) {
		this(null, code, desc);
    }

	public Restresult(R result, int code, String desc) {
        this.code = code;
        this.data = result;
        this.desc = desc;
    }

	public int getCode() {
		return code;
	}

	public R getData() {
		return data;
	}

	public String getDesc() {
		return desc;
	}
}
