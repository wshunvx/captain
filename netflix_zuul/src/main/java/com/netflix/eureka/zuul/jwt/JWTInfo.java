package com.netflix.eureka.zuul.jwt;

import java.io.Serializable;
import java.util.List;

import com.netflix.eureka.http.jwt.IJWTInfo;

public class JWTInfo implements Serializable, IJWTInfo {
	private static final long serialVersionUID = -1433617972421094362L;
	
    private String id;
	private String name;
	
	private List<String> metadata;
    
    public JWTInfo(String name, String id, List<String> metadata) {
    	this.name = name;
        this.id = id;
        this.metadata = metadata;
    }
    
    @Override
    public String getUniqueName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
	public String getMetadata() {
    	if(metadata == null || metadata.isEmpty()) {
    		return "";
    	}
    	return String.join("&&", metadata);
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JWTInfo jwtInfo = (JWTInfo) o;

        if (name != null ? !name.equals(jwtInfo.name) : jwtInfo.name != null) {
            return false;
        }
        
        return id != null ? id.equals(jwtInfo.id) : jwtInfo.id == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
