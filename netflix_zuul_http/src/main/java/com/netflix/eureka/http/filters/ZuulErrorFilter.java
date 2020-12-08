package com.netflix.eureka.http.filters;

import org.springframework.cloud.netflix.zuul.filters.post.SendErrorFilter;
import org.springframework.http.HttpStatus;
import org.springframework.util.ReflectionUtils;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.netflix.eureka.http.constants.ZuulConstant;
import com.netflix.eureka.http.utils.EntryUtils;
import com.netflix.zuul.context.RequestContext;

public class ZuulErrorFilter extends SendErrorFilter {

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		// only forward to errorPath if it hasn't been forwarded to already
		return ctx.getThrowable() != null
				&& !ctx.getBoolean(SEND_ERROR_FILTER_RAN, false);
	}

	@Override
	public Object run() {
		try {
			RequestContext ctx = RequestContext.getCurrentContext();
			Throwable throwable = ctx.getThrowable();
			if (throwable != null) {
				// Trace exception for each entry and exit entries in order.
                // The entries can be retrieved from the request context.
                if (ctx.containsKey(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY)) {
                	EntryUtils holders = getClassInfo(ctx, ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY, EntryUtils.class);
                	EntryUtils.EntryHolder holder;
                    while (!holders.isEmpty()) {
                        holder = holders.pop();
                        Tracer.traceEntry(throwable, holder.getEntry());
                        exit(holder);
                    }
                    ctx.remove(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY);
                }
                RecordLog.info("[ZuulErrorFilter] Trace error cause", throwable.getCause());
                
	            ExceptionHolder exception = findZuulException(ctx.getThrowable());
	            String errMsg = exception.getErrorCause();
	            if(errMsg != null) {
	            	ctx.setResponseBody(errMsg);
	            }
	            ctx.setResponseStatusCode(exception.getStatusCode());
	        } else {
	        	ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
	        }
			ctx.getResponse().setContentType("application/json; charset=utf-8");
		}
		catch (Exception ex) {
			ReflectionUtils.rethrowRuntimeException(ex);
		}
		
		ContextUtil.exit();
		return null;
	}
	
	<T> T getClassInfo(RequestContext requestContext, String clsname, Class<T> clazz) {
    	T t = null;
    	Object obj= requestContext.get(clsname);
    	if(obj != null) {
    		if(clazz.isAssignableFrom(obj.getClass())) {
    			t = clazz.cast(obj);
    		}
    	}
    	return t;
    }
	
	void exit(EntryUtils.EntryHolder holder) {
        Entry entry = holder.getEntry();
        entry.exit(1, holder.getParams());
    }
}
