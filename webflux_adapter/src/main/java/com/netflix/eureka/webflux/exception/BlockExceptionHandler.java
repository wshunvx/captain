package com.netflix.eureka.webflux.exception;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpLogging;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.function.Supplier;
import com.netflix.eureka.webflux.callback.WebFluxCallbackManager;

import reactor.core.publisher.Mono;

public class BlockExceptionHandler implements ErrorWebExceptionHandler, InitializingBean {
	private static final Log logger = HttpLogging.forLogName(BlockExceptionHandler.class);
	
    private List<ViewResolver> viewResolvers;
    private List<HttpMessageWriter<?>> messageWriters;
    private List<HttpMessageReader<?>> messageReaders = Collections.emptyList();
    /**
	 * Currently duplicated from Spring WebFlux HttpWebHandlerAdapter.
	 */
	private static final Set<String> DISCONNECTED_CLIENT_EXCEPTIONS;

	static {
		Set<String> exceptions = new HashSet<>();
		exceptions.add("AbortedException");
		exceptions.add("ClientAbortException");
		exceptions.add("EOFException");
		exceptions.add("EofException");
		DISCONNECTED_CLIENT_EXCEPTIONS = Collections.unmodifiableSet(exceptions);
	}

    public BlockExceptionHandler(List<ViewResolver> viewResolvers, ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolvers;
        this.messageWriters = serverCodecConfigurer.getWriters();
    }

    private Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange) {
        return response.writeTo(exchange, contextSupplier.get());
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    	if (exchange.getResponse().isCommitted() || isDisconnectedClientError(ex)) {
			return Mono.error(ex);
		}
    	
        // This exception handler only handles rejection by Security.
        if (!BlockException.isBlockException(ex)) {
            return Mono.error(ex);
        }
        
        ServerRequest request = ServerRequest.create(exchange, this.messageReaders);
        return handleBlockedRequest(exchange, ex).switchIfEmpty(Mono.error(ex))
        	.doOnNext((response) -> logError(request, response, ex))
            .flatMap(response -> writeResponse(response, exchange));
    }
    
    private boolean isDisconnectedClientError(Throwable ex) {
		return DISCONNECTED_CLIENT_EXCEPTIONS.contains(ex.getClass().getSimpleName())
				|| isDisconnectedClientErrorMessage(NestedExceptionUtils.getMostSpecificCause(ex).getMessage());
	}

    private boolean isDisconnectedClientErrorMessage(String message) {
		message = (message != null) ? message.toLowerCase() : "";
		return (message.contains("broken pipe") || message.contains("connection reset by peer"));
	}
    
    /**
	 * Configure HTTP message readers to deserialize the request body with.
	 * @param messageReaders the {@link HttpMessageReader}s to use
	 */
	public void setMessageReaders(List<HttpMessageReader<?>> messageReaders) {
		Assert.notNull(messageReaders, "'messageReaders' must not be null");
		this.messageReaders = messageReaders;
	}
	
	/**
	 * Logs the {@code throwable} error for the given {@code request} and {@code response}
	 * exchange. The default implementation logs all errors at debug level. Additionally,
	 * any internal server error (500) is logged at error level.
	 * @param request the request that was being handled
	 * @param response the response that was being sent
	 * @param throwable the error to be logged
	 * @since 2.2.0
	 */
	protected void logError(ServerRequest request, ServerResponse response, Throwable throwable) {
		if (logger.isDebugEnabled()) {
			logger.debug(request.exchange().getLogPrefix() + formatError(throwable, request));
		}
		if (HttpStatus.resolve(response.rawStatusCode()) != null
				&& response.statusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
			logger.error(LogMessage.of(() -> String.format("%s 500 Server Error for %s",
					request.exchange().getLogPrefix(), formatRequest(request))), throwable);
		}
	}
	
	private String formatRequest(ServerRequest request) {
		String rawQuery = request.uri().getRawQuery();
		String query = StringUtils.hasText(rawQuery) ? "?" + rawQuery : "";
		return "HTTP " + request.methodName() + " \"" + request.path() + query + "\"";
	}
	
	private String formatError(Throwable ex, ServerRequest request) {
		String reason = ex.getClass().getSimpleName() + ": " + ex.getMessage();
		return "Resolved [" + reason + "] for HTTP " + request.methodName() + " " + request.path();
	}
	
    private Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable) {
        return WebFluxCallbackManager.getBlockHandler().handleRequest(exchange, throwable);
    }

    private final Supplier<ServerResponse.Context> contextSupplier = () -> new ServerResponse.Context() {
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return BlockExceptionHandler.this.messageWriters;
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return BlockExceptionHandler.this.viewResolvers;
        }
    };

	@Override
	public void afterPropertiesSet() throws Exception {
		if (CollectionUtils.isEmpty(this.messageWriters)) {
			throw new IllegalArgumentException("Property 'messageWriters' is required");
		}
	}
}
