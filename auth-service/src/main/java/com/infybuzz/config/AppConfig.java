package com.infybuzz.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
@Data
public class AppConfig {

    @Value("${application.jwt.accessTokenExpiration}")
    private Long accessTokenExpiration;

    @Value("${application.jwt.refreshTokenExpiration}")
    private Long refreshTokenExpiration;

    @Value("${spring.security.oauth2.authorization-server.jwt.issuer-uri}")
    private String jwtIssuerUri;
}
