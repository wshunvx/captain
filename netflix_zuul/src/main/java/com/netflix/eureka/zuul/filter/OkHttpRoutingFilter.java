package com.netflix.eureka.zuul.filter;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.LOAD_BALANCER_KEY;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.RETRYABLE_KEY;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_KEY;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.ribbon.support.RibbonCommandContext;
import org.springframework.cloud.netflix.ribbon.support.RibbonRequestCustomizer;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommand;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonRoutingFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.netflix.eureka.http.constants.ZuulConstant;
import com.netflix.eureka.http.jwt.IJWTInfo;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@SuppressWarnings("rawtypes")
public class OkHttpRoutingFilter extends RibbonRoutingFilter {
	private static final Logger log = LoggerFactory.getLogger(RibbonRoutingFilter.class);
	
	protected ProxyRequestHelper helper;
	
	protected RibbonCommandFactory<?> ribbonCommandFactory;

	protected List<RibbonRequestCustomizer> requestCustomizers;
	
	public OkHttpRoutingFilter(ProxyRequestHelper helper, RibbonCommandFactory<?> ribbonCommandFactory,
			List<RibbonRequestCustomizer> requestCustomizers) {
		super(helper, ribbonCommandFactory, requestCustomizers);
		this.helper = helper;
		this.ribbonCommandFactory = ribbonCommandFactory;
		this.requestCustomizers = requestCustomizers;
	}
	
	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return (ctx.getRouteHost() == null && ctx.get(SERVICE_ID_KEY) != null
				&& ctx.sendZuulResponse());
	}

	@Override
	public Object run() {
		RequestContext requestContext = RequestContext.getCurrentContext();
		try {
			this.helper.addIgnoredHeaders();
			RibbonCommandContext commandContext = buildCommandContext(requestContext);
			ClientHttpResponse response = forward(commandContext);
			
			InputStream inputStream = response.getBody();
			RequestContext.getCurrentContext().set("zuulResponse", response);
			this.helper.setResponse(response.getRawStatusCode(), inputStream, response.getHeaders());
			
	        return response;
		} catch (Exception e) {
			ReflectionUtils.rethrowRuntimeException(e);
		}
		return null;
	}
	
	protected RibbonCommandContext buildCommandContext(RequestContext context) {
		HttpServletRequest request = context.getRequest();

		MultiValueMap<String, String> headers = this.helper
				.buildZuulRequestHeaders(request);
		MultiValueMap<String, String> params = this.helper
				.buildZuulRequestQueryParams(request);
		String verb = getVerb(request);

		String serviceId = (String) context.get(SERVICE_ID_KEY);
		Boolean retryable = (Boolean) context.get(RETRYABLE_KEY);
		Object loadBalancerKey = context.get(LOAD_BALANCER_KEY);
		
		InputStream requestEntity = getRequestBody(request);
		if (request.getContentLength() < 0 && !verb.equalsIgnoreCase("GET")) {
			context.setChunkedRequestBody();
		}
		
		long contentLength = request.getContentLengthLong();
		
		try {
			IJWTInfo iJWTInfo = getClassInfo(context, "iJWTInfo", IJWTInfo.class);
			if(iJWTInfo != null) {
				headers.add(ZuulConstant.AUTHORITY_TYPE_USER, iJWTInfo.getId());
			}
		} catch (Exception ex) {
			log.error("Error during rsaKeyHelper", ex);
		}
		
		String uri = this.helper.buildZuulRequestURI(request);

		uri = uri.replace("//", "/");

		return new RibbonCommandContext(serviceId, verb, uri, retryable, headers, params,
				requestEntity, this.requestCustomizers, contentLength, loadBalancerKey);
	}
	
	protected Date getValidtime(int expire) {
    	return DateTime.now().plusSeconds(expire).toDate();
    }
	
	protected boolean permitsContentType(String contentType) {
		if(StringUtils.isEmpty(contentType)) {
			return true;
		}
		
		return !(contentType.startsWith("multipart/form-data") || contentType.startsWith("multipart/mixed"));
	}
	
	protected ClientHttpResponse forward(RibbonCommandContext context) throws HystrixRuntimeException, IOException {
		RibbonCommand command = this.ribbonCommandFactory.create(context);
		ClientHttpResponse response = command.execute();
		return response;
	}

    private <T> T getClassInfo(RequestContext requestContext, String clsname, Class<T> clazz) {
    	T t = null;
    	Object iJWTInfo= requestContext.get(clsname);
    	if(iJWTInfo != null) {
    		if(clazz.isAssignableFrom(iJWTInfo.getClass())) {
    			t = clazz.cast(iJWTInfo);
    		}
    	}
    	return t;
    }

}
