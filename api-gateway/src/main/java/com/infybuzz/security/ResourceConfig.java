package com.infybuzz.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.infybuzz.security.Role.SHOP_OWNER;
import static com.infybuzz.security.Role.USER;

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
public class ResourceConfig {
    Logger logger = LoggerFactory.getLogger(ResourceConfig.class);

    @Autowired
    private JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // Emulate SessionCreationPolicy.STATELESS

                .authorizeExchange(exchanges -> exchanges
                        .matchers(this::internalCall).permitAll() // Skip authentication for internal API Calls
                        .pathMatchers("/eureka/**").permitAll()
                        .pathMatchers("/auth-service/api/auth/**").permitAll()

                        // Beverage service endpoints
                        .pathMatchers(HttpMethod.GET, "/beverage-service/api/beverage/getById/**").hasRole(USER.name())
                        .pathMatchers(HttpMethod.GET, "/beverage-service/api/beverage/getAll/**").hasRole(USER.name())
                        .pathMatchers(HttpMethod.POST, "/beverage-service/api/beverage/**").hasRole(SHOP_OWNER.name())
                        .pathMatchers(HttpMethod.PUT, "/beverage-service/api/beverage/**").hasRole(SHOP_OWNER.name())
                        .pathMatchers(HttpMethod.DELETE, "/beverage-service/api/beverage/**").hasRole(SHOP_OWNER.name())

                        // Order service endpoints
                        .pathMatchers(HttpMethod.GET, "/order-service/api/order/getById/**").hasRole(USER.name())
                        .pathMatchers(HttpMethod.GET, "/order-service/api/order/getAll").hasRole(SHOP_OWNER.name())
                        .pathMatchers(HttpMethod.POST, "/order-service/api/order/place").hasRole(USER.name())
                        .pathMatchers(HttpMethod.PUT, "/order-service/api/order/**").hasAnyRole(USER.name(), SHOP_OWNER.name())
                        .pathMatchers(HttpMethod.DELETE, "/order-service/api/order/**").hasRole(SHOP_OWNER.name())

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );

        return http.build();
    }

    private Mono<MatchResult> internalCall(ServerWebExchange exchange) {
        logger.info("Inside internalCall");

        // Display all the available headers
        exchange.getRequest().getHeaders()
                .forEach((key, value) -> logger.info(key + "= " + value));

        // Check if the X-Internal-Request header has the value "Internal"
        HttpHeaders headers = exchange.getRequest().getHeaders();
        if (headers.containsKey("X-Internal-Request")) {
            if ("Internal".equalsIgnoreCase(headers.getFirst("X-Internal-Request"))) {
                return MatchResult.match();
            }
        }

        // Default to not match if the header is not present or does not have the expected value
        return MatchResult.notMatch();
    }

}