package com.gen.auth.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.netflix.eureka.http.NettyHttpServer;

@NettyHttpServer
@SpringBootApplication
@MapperScan("com.gen.auth.server.mapper")
@EnableAutoConfiguration
public class AuthBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(AuthBootstrap.class, args);
    }
}
