package com.netflix.eureka.bean;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ZRsa {
    private volatile String password;

    private volatile Long ts;
    private volatile Map<String, Collection<ZUri>> uri;
    
    public ZRsa(Long ts) {
		this.ts = ts;
	}
    
    public static final class Builder {
    	private ZRsa rsa;
    	
    	private Multimap<String, ZUri> map;
    	
    	private Builder(ZRsa rsa) {
    		this.rsa = rsa;
    		this.map = ArrayListMultimap.create();
    	}
    	
    	public static Builder newBuilder(Long ts) {
    		return new Builder(new ZRsa(ts));
    	}
    	
    	public Builder setPassword(String password) {
    		rsa.password = password;
            return this;
        }
    	
    	public Builder setTs(Long ts) {
    		rsa.ts = ts;
            return this;
        }
    	
    	public Builder setUri(String svrid, ZUri uri) {
    		map.put(svrid, uri);
            return this;
        }
    	
    	public Builder setUri(String svrid, List<ZUri> uri) {
    		map.putAll(svrid, uri);
            return this;
        }
    	
    	public Builder setUri(String svrid, String id, String basepath, Integer strategy, String method) {
    		return setUri(svrid, new ZUri(id, basepath, strategy, method));
        }
    	
    	public ZRsa build() {
    		rsa.uri = map.asMap();
    		return rsa;
    	}
    }

	public String getPassword() {
		return password;
	}

	public Long getTs() {
		return ts;
	}

	public Map<String, Collection<ZUri>> getUri() {
		return uri;
	}

}
