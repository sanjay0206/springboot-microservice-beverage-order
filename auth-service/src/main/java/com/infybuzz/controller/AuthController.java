package com.infybuzz.controller;

import com.infybuzz.entity.UserCredEntity;
import com.infybuzz.repository.UserCredRepository;
import com.infybuzz.request.AuthRequest;
import com.infybuzz.request.RefreshTokenRequest;
import com.infybuzz.security.CustomUserDetailsService;
import com.infybuzz.service.TokenService;
import com.infybuzz.utils.AuthUtils;
import com.nimbusds.jose.JOSEException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    Logger logger = LoggerFactory.getLogger(AuthController.class);


    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserCredRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwkSet() {
        logger.info("Inside getJwkSet");
        return tokenService.getJwkSet();
    }

    @PostMapping("/register")
    public ResponseEntity<UserCredEntity> registerUser(@RequestBody UserCredEntity user) {
        logger.info("Registering new user: " + user);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.ok(repository.save(user));
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> generateTokens(@RequestBody AuthRequest authRequest) throws JOSEException {
        logger.info("Generating tokens for user: {}", authRequest.getUsername());

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            List<SimpleGrantedAuthority> authorities = AuthUtils.convertToSimpleGrantedAuthorities(userDetails.getAuthorities());
            Map<String, String> tokens = tokenService.generateAccessAndRefreshTokens(authRequest.getUsername(), authorities);

            return ResponseEntity.ok(tokens);
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication failed"));
        }
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        logger.info("Refreshing token");

        try {
            Map<String, String> refreshToken = tokenService.refreshToken(refreshTokenRequest.getRefreshToken());
            return ResponseEntity.ok(refreshToken);
        } catch (JOSEException | ParseException e) {
            logger.error("Error refreshing token", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired refresh token"));
        } catch (RuntimeException e) {
            logger.error("Token validation failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

}
