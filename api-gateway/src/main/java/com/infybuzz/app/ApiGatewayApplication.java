package com.infybuzz.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bootstrap.encrypt.RsaProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@ComponentScan({"com.infybuzz.security"})
@EnableDiscoveryClient
@EnableConfigurationProperties(RsaProperties.class)
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}
