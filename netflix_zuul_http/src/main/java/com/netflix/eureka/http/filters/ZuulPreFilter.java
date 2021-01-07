package com.netflix.eureka.http.filters;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_KEY;

import java.security.KeyPair;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import com.netflix.eureka.command.CommandConstants;
import com.netflix.eureka.common.ApiDefinition;
import com.netflix.eureka.common.GatewayFlowRule;
import com.netflix.eureka.http.auth.UriCache;
import com.netflix.eureka.http.cache.IRouteCache;
import com.netflix.eureka.http.check.matcher.RequestContextApiMatcher;
import com.netflix.eureka.http.common.GatewayParamParser;
import com.netflix.eureka.http.common.RequestItemParser;
import com.netflix.eureka.http.constants.ZuulConstant;
import com.netflix.eureka.http.jwt.IJWTInfo;
import com.netflix.eureka.http.utils.EntryUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

public abstract class ZuulPreFilter extends ZuulFilter {

    private final GatewayParamParser<RequestContext> paramParser = new GatewayParamParser<>(
        new RequestContextItemParser());

    private final List<String> gatePrefix;
    
    protected IRouteCache routeLocator;
    protected UrlPathHelper urlPathHelper = new UrlPathHelper();

    protected UriCache uriCache;
    
    public ZuulPreFilter(UriCache uriCache, IRouteCache routeLocator, ZuulProperties properties, List<String> prefix) {
    	this.uriCache = uriCache;
    	this.routeLocator = routeLocator;
    	this.gatePrefix = prefix;
    	this.urlPathHelper.setRemoveSemicolonContent(properties.isRemoveSemicolonContent());
    	this.urlPathHelper.setUrlDecode(properties.isDecodeUrl());
    }
    
    abstract public IJWTInfo getJWTUser(KeyPair keyMap, String userToken);
    
    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
   	public boolean shouldFilter() {
       	RequestContext requestContext = RequestContext.getCurrentContext();
       	HttpServletRequest request = requestContext.getRequest();
       	String requestUri = request.getRequestURI();
           if (isStartWith(requestUri)) {
               return false;
           }
           
   		return true;
   	}

    @Override
    public Object run() throws ZuulException {
    	RequestContext ctx = RequestContext.getCurrentContext();
    	
        String routeId = null;
        EntryUtils holders = new EntryUtils();
        try {
        	HttpServletRequest request = ctx.getRequest();
        	LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    String value = values.nextElement();
                    headers.add(name, value);
                }
            }
            final String requestURI = urlPathHelper.getPathWithinApplication(request);
            Route route = routeLocator.getMatchingRoute(requestURI);
        	routeId = route.getId();
        	String origin = parseOrigin(route);
        	
            if (StringUtil.isNotBlank(routeId)) {
                ContextUtil.enter(CommandConstants.GATEWAY_CONTEXT_ROUTE_PREFIX + routeId, origin);
                doSecurityEntry(routeId, CommandConstants.RESOURCE_MODE_ROUTE_ID, ctx, holders);
            }

            Set<String> matchingApis = pickMatchingApiDefinitions(route);
            if (!matchingApis.isEmpty() && ContextUtil.getContext() == null) {
                ContextUtil.enter(ZuulConstant.ZUUL_DEFAULT_CONTEXT, origin);
            }
            for (String apiName : matchingApis) {
            	doSecurityEntry(apiName, CommandConstants.RESOURCE_MODE_CUSTOM_API_NAME, ctx, holders);
            }
            
			String authToken = headers.getFirst(ZuulConstant.AUTHORITY_TYPE.toLowerCase());
			if (StringUtils.isEmpty(authToken) || "undefined".equals(authToken)) {
				String strings = request.getParameter("token");
				if (strings != null) {
					authToken = strings;
				}
			}
			
			if(uriCache.verification(route.getLocation(), request.getMethod(), route)) {
				IJWTInfo iJWTInfo = getJWTUser(uriCache.keyPair(), authToken);
				if (iJWTInfo == null) {
					throw new ZuulException("Forbidden", HttpStatus.FORBIDDEN.value(), "User not logged in.");
				}
				ctx.put(ZuulConstant.AUTHORITY_TYPE_USER, iJWTInfo);
			}
        } catch (Exception ex) {
            // Prevent routing from running
            ctx.setThrowable(ex);
            ctx.setRouteHost(null);
            ctx.setSendZuulResponse(false);
            ctx.set(SERVICE_ID_KEY, null);
            ctx.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
        } finally {
        	// We don't exit the entry here. We need to exit the entries in post filter to record Rt correctly.
            // So here the entries will be carried in the request context.
            if (!holders.isEmpty()) {
                ctx.put(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY, holders);
            }
        }
        return null;
    }
    
    private void doSecurityEntry(String resourceName, final int resType, RequestContext requestContext,
    		EntryUtils holders) throws BlockException {
        Object[] params = paramParser.parseParameterFor(resourceName, requestContext,
            new Predicate<GatewayFlowRule>() {
                @Override
                public boolean test(GatewayFlowRule r) {
                    return r.getResourceMode() == resType;
                }
            });
        AsyncEntry entry = SphU.asyncEntry(resourceName, ResourceTypeConstants.COMMON_API_GATEWAY,
                EntryType.IN, params);
        holders.setHolder(entry, params);
    }
    
    private String parseOrigin(Route route) {
		if(route != null) {
			return route.getLocation();
		}
        return "";
    }

    /**
     * warn!!!
     * Cache the results
     * @param requestContext
     * @return
     */
    private Set<String> pickMatchingApiDefinitions(Route route) {
    	Set<String> apis = new HashSet<>();
    	routeLocator.getLocateRoutes().forEach((path, zuul) -> {
    		ApiDefinition definition = new ApiDefinition();
			definition.setApiName(zuul.getId());
			definition.setServiceId(zuul.getServiceId());
			
			definition.setUrl(zuul.getUrl());
			definition.setPattern(zuul.getPath());
			definition.setMatchStrategy(CommandConstants.URL_MATCH_STRATEGY_PREFIX);
			
			RequestContextApiMatcher matcher = new RequestContextApiMatcher(definition);
            if (matcher.test(route)) {
                apis.add(matcher.getApiName());
            }
    	});
        return apis;
    }
    
    private boolean isStartWith(String requestUri) {
    	String path = requestUri;
    	if (path.endsWith("/")) {
    		path = path.substring(0, path.length() - 1);
		}
    	
    	if(gatePrefix.contains(path)) {
    		return true;
    	}
    	
    	String[] index = path.split("/");
    	index[index.length -1] = "*";
        return gatePrefix.contains(String.join("/", index));
    }
    
    class RequestContextItemParser implements RequestItemParser<RequestContext> {

        @Override
        public String getPath(RequestContext requestContext) {
            return requestContext.getRequest().getServletPath();
        }

        @Override
        public String getRemoteAddress(RequestContext requestContext) {
            return requestContext.getRequest().getRemoteAddr();
        }

        @Override
        public String getHeader(RequestContext requestContext, String headerKey) {
            return requestContext.getRequest().getHeader(headerKey);
        }

        @Override
        public String getUrlParam(RequestContext requestContext, String paramName) {
            return requestContext.getRequest().getParameter(paramName);
        }

        @Override
        public String getCookieValue(RequestContext requestContext, String cookieName) {
            Cookie[] cookies = requestContext.getRequest().getCookies();
            if (cookies == null || cookieName == null) {
                return null;
            }
            for (Cookie cookie : cookies) {
                if (cookie != null && cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
            return null;
        }
    }
}
