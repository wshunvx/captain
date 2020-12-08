package com.netflix.eureka.found.transport;

import java.io.IOException;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.transport.builder.JacksonBuilder;
import com.netflix.eureka.found.transport.http.RestTemplateAuthHttpClient;
import com.netflix.eureka.found.transport.rule.AuthRule;

public class AuthTransportClientFactory {
	private final static String AUTHORITY_TOKEN = "Atoken";
	private final static String AUTHORITY_SERVICE = "Svrid";
    
	public AuthHttpClient newClient(AuthRule aRule) {
		return new RestTemplateAuthHttpClient(restTemplate(), aRule);
	}

	private RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(0, mappingJacksonHttpMessageConverter());
		restTemplate.setErrorHandler(new ErrorHandler());
		restTemplate.setInterceptors(Collections.singletonList(new HeaderRequestInterceptor()));
		return restTemplate;
	}

	public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(new ObjectMapper()
				.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE));

//		converter.getObjectMapper().configure(SerializationFeature.WRAP_ROOT_VALUE, true);
//		converter.getObjectMapper().configure(DeserializationFeature.UNWRAP_ROOT_VALUE,
//				true);
		converter.getObjectMapper().addMixIn(Restresult.class,
				JacksonBuilderMixIn.class);
//		converter.getObjectMapper().addMixIn(Namespace.class,
//				DataJsonMixIn.class);
		return converter;
	}

	@JsonDeserialize(builder = JacksonBuilder.class)
	class JacksonBuilderMixIn {
		
	}
	
	class ErrorHandler extends DefaultResponseErrorHandler {

		@Override
		protected boolean hasError(HttpStatus statusCode) {
			if (statusCode.is4xxClientError()) {
				return false;
			}
			return super.hasError(statusCode);
		}

	}
	
	class HeaderRequestInterceptor implements ClientHttpRequestInterceptor {

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			HttpHeaders headers = request.getHeaders();
            headers.add(AUTHORITY_TOKEN, "Atoken");
            headers.add(AUTHORITY_SERVICE, "Svrid");
            return execution.execute(request, body);
		}
		
	}
}
