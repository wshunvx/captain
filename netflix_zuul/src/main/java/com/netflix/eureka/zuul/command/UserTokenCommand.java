package com.netflix.eureka.zuul.command;

import java.util.stream.Stream;

import com.netflix.eureka.http.jwt.IJWTInfo;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public abstract class UserTokenCommand extends HystrixCommand<IJWTInfo> {

	private final String token;
	private final String serviceId;
	private final String serviceType;
	
	protected abstract IJWTInfo fromToken(String token) throws Exception;
	protected abstract Stream<String> fromPermissinon(IJWTInfo iJWTInfo, String serviceId, String serviceType);
	
	public UserTokenCommand(String authToken, String serviceId, String serviceType) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("Token")));
        this.token = authToken;
        this.serviceId = serviceId;
        this.serviceType = serviceType;
    }
	
	@Override
	protected IJWTInfo run() throws Exception {
		IJWTInfo iJWTInfo = fromToken(token);
		if(iJWTInfo == null || iJWTInfo.getMetadata() == null) {
			return null;
		}
		
		Stream<String> permissinon = fromPermissinon(iJWTInfo, serviceId, serviceType);
		if(permissinon == null || permissinon.count() == 0) {
			return null;
		}
		
		return iJWTInfo;
	}
	@Override
    protected IJWTInfo getFallback() {
        return null;
    }

}
