package com.netflix.eureka.bean;

public class ZUri {
	private final String id;
    private final String method;
    private final String basepath;
    
    private final Integer strategy;

    public ZUri(String id, String basepath, Integer strategy, String method) {
    	this.id = id;
        this.basepath = basepath;
        this.strategy = strategy;
        this.method = method;
    }

	public String getId() {
		return id;
	}

	public String getBasepath() {
		return basepath;
	}

	public Integer getStrategy() {
		return strategy;
	}

	public String getMethod() {
		return method;
	}
	
}
