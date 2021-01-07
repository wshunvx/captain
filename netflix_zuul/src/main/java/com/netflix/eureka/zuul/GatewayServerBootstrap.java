package com.netflix.eureka.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.netflix.eureka.http.NettyHttpServer;
import com.netflix.eureka.http.ZuulHttpServer;

@ZuulHttpServer
@NettyHttpServer
@EnableDiscoveryClient
//@EnableFeignClients({"com.netflix.eureka.http.feign"})
@SpringBootApplication
public class GatewayServerBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServerBootstrap.class, args);
    }
}
