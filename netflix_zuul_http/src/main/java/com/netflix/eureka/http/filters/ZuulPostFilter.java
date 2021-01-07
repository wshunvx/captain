package com.netflix.eureka.http.filters;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;

import java.io.ByteArrayInputStream;
import java.net.SocketTimeoutException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.util.ReflectionUtils;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.netflix.client.ClientException;
import com.netflix.eureka.command.CommandResponse;
import com.netflix.eureka.http.constants.ZuulConstant;
import com.netflix.eureka.http.utils.EntryUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

public class ZuulPostFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
    	try {
	        // Exit the entries in order.
	        // The entries can be retrieved from the request context.
	    	RequestContext ctx = RequestContext.getCurrentContext();
	    	Throwable exception = ctx.getThrowable();
			if(exception != null) {
				HttpServletResponse response = ctx.getResponse();
				String message = findZuulExceptionMessage(exception);
				if(message != null) {
					ctx.setResponseDataStream(new ByteArrayInputStream(message.getBytes()));
					ServletOutputStream outStream = response.getOutputStream();
					outStream.write(CommandResponse.ofFailure(500, message).toString().getBytes());
				}
	            response.setContentType("application/json; charset=utf-8");
			}
			
	        if (ctx.containsKey(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY)) {
	        	EntryUtils holders = getClassInfo(ctx, ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY, EntryUtils.class);
	        	EntryUtils.EntryHolder holder;
	            while (!holders.isEmpty()) {
	                holder = holders.pop();
	                exit(holder);
	            }
	            ctx.remove(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY);
	        }
    	} catch (Exception ex) {
			ReflectionUtils.rethrowRuntimeException(ex);
		}
        ContextUtil.exit();
        return null;
    }
    
    protected String findZuulExceptionMessage(Throwable throwable) {
		if (throwable.getCause() instanceof ZuulRuntimeException) {
			Throwable cause = null;
			if (throwable.getCause().getCause() != null) {
				cause = throwable.getCause().getCause().getCause();
			}
			if (cause instanceof ClientException && cause.getCause() != null
					&& cause.getCause().getCause() instanceof SocketTimeoutException) {
				return ZuulException.class.getName() + ": Hystrix Readed time out";
			}
			
			if (throwable.getCause().getCause() instanceof ZuulException) {
				return ((ZuulException) throwable.getCause().getCause()).errorCause;
			}
		}

		if (throwable.getCause() instanceof ZuulException) {
			return ((ZuulException) throwable.getCause()).errorCause;
		}

		if (throwable instanceof ZuulException) {
			return ((ZuulException) throwable).errorCause;
		}

		return throwable.getMessage();
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
