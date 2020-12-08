package com.netflix.eureka.zuul.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.ribbon.support.RibbonRequestCustomizer;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonRoutingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.eureka.command.CommandConstants;
import com.netflix.eureka.common.GatewayFlowRule;
import com.netflix.eureka.common.GatewayParamFlowItem;
import com.netflix.eureka.http.common.rule.GatewayRuleManager;
import com.netflix.eureka.http.filters.ZuulErrorFilter;
import com.netflix.eureka.http.filters.ZuulPostFilter;
import com.netflix.eureka.zuul.filter.AccessGatewayFilter;
import com.netflix.eureka.zuul.filter.OkHttpRoutingFilter;
import com.netflix.eureka.zuul.msg.ZuulFallbackProvider;

@Configuration
public class GatewayConfig {
	
	@SuppressWarnings("rawtypes")
	@Autowired(required = false)
	private List<RibbonRequestCustomizer> requestCustomizers = Collections.emptyList();
	
	@PostConstruct
    public void doInit() {
        // Prepare some gateway rules and API definitions (only for demo).
        // It's recommended to leverage dynamic data source or the Sentinel dashboard to push the rules.
//        initCustomizedApis();
        initGatewayRules();
    }
//
//    private void initCustomizedApis() {
//        List<ApiDefinition> definitions = new ArrayList<>();
//        ApiDefinition api1 = new ApiDefinition("some_customized_api")
//            .setPredicateItems(new ArrayList<ApiPredicateItem>() {{
//                add(new ApiPathPredicateItem().setPattern("/ahas"));
//                add(new ApiPathPredicateItem().setPattern("/aliyun_product/**")
//                    .setMatchStrategy(CommandConstants.URL_MATCH_STRATEGY_PREFIX));
//            }});
//        ApiDefinition api2 = new ApiDefinition("another_customized_api")
//            .setPredicateItems(new ArrayList<ApiPredicateItem>() {{
//                add(new ApiPathPredicateItem().setPattern("/**")
//                    .setMatchStrategy(CommandConstants.URL_MATCH_STRATEGY_PREFIX));
//            }});
//        definitions.add(api1);
//        definitions.add(api2);
//        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
//    }
//
    private void initGatewayRules() {
    	List<GatewayFlowRule> rules = new ArrayList<>();
        rules.add(new GatewayFlowRule("all-product-route")
            .setCount(100)
            .setIntervalSec(1)
        );
        rules.add(new GatewayFlowRule("aliyun-product-route")
                .setCount(2)
                .setIntervalSec(2)
                .setBurst(2)
                .setParamItem(new GatewayParamFlowItem()
                    .setParseStrategy(CommandConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
                )
            );
//        rules.add(new GatewayFlowRule("another-route-httpbin")
//            .setCount(10)
//            .setIntervalSec(1)
//            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
//            .setMaxQueueingTimeoutMs(600)
//            .setParamItem(new GatewayParamFlowItem()
//                .setParseStrategy(CommandConstants.PARAM_PARSE_STRATEGY_HEADER)
//                .setFieldName("X-Sentinel-Flag")
//            )
//        );
//        rules.add(new GatewayFlowRule("another-route-httpbin")
//            .setCount(1)
//            .setIntervalSec(1)
//            .setParamItem(new GatewayParamFlowItem()
//                .setParseStrategy(CommandConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
//                .setFieldName("pa")
//            )
//        );
//
    	rules.add(new GatewayFlowRule("demo123")
    			.setResourceMode(CommandConstants.RESOURCE_MODE_CUSTOM_API_NAME)
    			.setCount(15)
    			.setIntervalSec(1));
//        rules.add(new GatewayFlowRule("demo123")
//            .setResourceMode(CommandConstants.RESOURCE_MODE_CUSTOM_API_NAME)
//            .setCount(2)
//            .setIntervalSec(1)
//            .setParamItem(new GatewayParamFlowItem()
//                .setParseStrategy(CommandConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
//                .setFieldName("pn")
//            )
//        );
        GatewayRuleManager.loadRules(rules);
    }
    
	@Bean AccessGatewayFilter accessGatewayFilter(RouteLocator routeLocator, ZuulProperties zuulProperties,
			@Value("${gate.ignore.prefix}") List<String> prefix) {
		return new AccessGatewayFilter(routeLocator, zuulProperties, prefix);
	}
	
	@Bean RibbonRoutingFilter okHttpRoutingFilter(ProxyRequestHelper helper,
			RibbonCommandFactory<?> ribbonCommandFactory) {
		return new OkHttpRoutingFilter(helper, ribbonCommandFactory, this.requestCustomizers);
	}
	
	@Bean ZuulPostFilter getZuulPostFilter() {
        return new ZuulPostFilter();
    }
	
	@Bean ZuulFallbackProvider getZuulFallbackProvider() {
        return new ZuulFallbackProvider();
    }
	
	@Bean ZuulErrorFilter getZuulErrorFilter() {
        return new ZuulErrorFilter();
    }

}
