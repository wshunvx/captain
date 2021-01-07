package com.netflix.eureka.http.auth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.netflix.ribbon.support.RibbonCommandContext;
import org.springframework.cloud.netflix.ribbon.support.RibbonRequestCustomizer;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommand;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;

import com.google.gson.JsonObject;
import com.netflix.eureka.bean.ZRsa;
import com.netflix.eureka.bean.ZUri;
import com.netflix.eureka.gson.JSONFormatter;
import com.netflix.hystrix.exception.HystrixRuntimeException;

public class ZuulServiceRunner implements CommandLineRunner {
	private static Logger logger = LoggerFactory.getLogger(ZuulServiceRunner.class);

	private String name = "gen-auth-service";
    
    protected Timer serverWeightTimer = null;

    @SuppressWarnings("rawtypes")
	@Autowired(required = false)
	private List<RibbonRequestCustomizer> requestCustomizers = Collections.emptyList();
    
    protected final int DEFAULT_TIMER_INTERVAL = 15 * 1000;
    protected volatile Long ts = 0L;
    
    @Autowired
    private UriCache uriCache;
    @Autowired
    private RibbonCommandFactory<?> ribbonCommandFactory;
    
	@Override
	public void run(String... args) throws Exception {
		serverWeightTimer = new Timer("ChooseRule-serverWeightTimer-"
                + name, true);
        serverWeightTimer.schedule(new DynamicServerWeightTask(), 0,
        		DEFAULT_TIMER_INTERVAL);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
            	logger.info("End of the process chooseRule-serverWeightTimer-{}", name);
                serverWeightTimer.cancel();
            }
        }));
		
	}
	
	public void shutdown() {
        if (serverWeightTimer != null) {
        	logger.info("Stopping ChooseRule-serverWeightTimer-{}", name);
            serverWeightTimer.cancel();
        }
        serverWeightTimer = null;
    }
	
	public void refreshUserPubKey(){
		try {
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
			headers.add("Content-Type", "application/json");
			MultiValueMap<String, String> params  = new LinkedMultiValueMap<>();
			params.add("svrid", "");
			params.add("ts", String.valueOf(ts));
			
			InputStream requestEntity = new ByteArrayInputStream(new byte[0]);
			RibbonCommandContext commandContext = new RibbonCommandContext("gen-auth", "POST", "/zuul/rsa", false, headers, params, requestEntity, requestCustomizers, null);
			ClientHttpResponse response = forward(commandContext);
			if(response.getRawStatusCode() == 200) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				StreamUtils.copy(response.getBody(), out);
				JsonObject body = JSONFormatter.fromJSON(new String(out.toByteArray()), JsonObject.class);
				if(body != null && body.get("code").getAsInt() == 1000) {
					ZRsa rsa = JSONFormatter.fromJSON(body.get("data"), ZRsa.class);
					if(rsa != null) {
						uriCache.init(rsa.getPassword());
						Map<String, Collection<ZUri>> list = rsa.getUri();
						if(!(list == null || list.isEmpty())) {
							list.forEach((key, ls) -> uriCache.invalidate(key, ts, ls));
						}
						ts = rsa.getTs();
					}
				}
			}
		} catch (HystrixRuntimeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public ClientHttpResponse forward(RibbonCommandContext context) throws HystrixRuntimeException, IOException {
		RibbonCommand command = this.ribbonCommandFactory.create(context);
		ClientHttpResponse response = command.execute();
		return response;
	}
	
	class DynamicServerWeightTask extends TimerTask {
        public void run() {
            try {
            	refreshUserPubKey();
            } catch (Exception e) {
                logger.error("Error running DynamicServerWeightTask for {}, {}", name, e.getMessage());
            }
        }
    }
	
}
