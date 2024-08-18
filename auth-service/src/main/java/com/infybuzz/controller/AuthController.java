package com.infybuzz.controller;

import com.infybuzz.entity.UserCredEntity;
import com.infybuzz.repository.UserCredRepository;
import com.infybuzz.request.AuthRequest;
import com.infybuzz.request.RefreshTokenRequest;
import com.infybuzz.security.CustomUserDetailsService;
import com.infybuzz.service.TokenService;
import com.infybuzz.utils.AuthUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private AuthenticationManager authenticationManager;

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
    public UserCredEntity registerUser(@RequestBody UserCredEntity user) {
        logger.info("Registering new user: " + user);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        return repository.save(user);
    }

    @PostMapping("/token")
    public Map<String, String> generateTokens(@RequestBody AuthRequest authRequest) throws JOSEException {
        logger.info("Generating tokens for user: {}", authRequest.getUsername());

        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        authenticateUser(authRequest.getUsername(), authRequest.getPassword(), userDetails);

        List<SimpleGrantedAuthority> authorities = AuthUtils.convertToSimpleGrantedAuthorities(userDetails.getAuthorities());
        return tokenService.generateAccessAndRefreshTokens(authRequest.getUsername(), authorities);
    }

    @PostMapping("/refresh-token")
    public Map<String, String> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) throws JOSEException, ParseException {
        logger.info("Refreshing token");

        SignedJWT signedJWT = SignedJWT.parse(refreshTokenRequest.getRefreshToken());
        String username = signedJWT.getJWTClaimsSet().getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        authenticateUser(username, userDetails.getPassword(), userDetails);

        List<SimpleGrantedAuthority> authorities = AuthUtils.convertToSimpleGrantedAuthorities(userDetails.getAuthorities());
        return tokenService.generateNewRefreshToken(signedJWT, authorities);
    }

    private void authenticateUser(String username, String password, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                username,
                password,
                userDetails.getAuthorities()
        );

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        if (!authentication.isAuthenticated()) {
            throw new RuntimeException("Authentication failed");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
