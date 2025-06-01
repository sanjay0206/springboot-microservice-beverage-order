package com.infybuzz.controller;

import com.infybuzz.request.AuthRequest;
import com.infybuzz.request.RefreshTokenRequest;
import com.infybuzz.request.UserRequest;
import com.infybuzz.service.AuthService;
import com.nimbusds.jose.JOSEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwkSet() {
        log.info("Inside getJwkSet");
        return authService.getJwkSet();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRequest userRequest) {
        log.info("Registering new user: {}", userRequest);

        return ResponseEntity.ok(authService.registerUser(userRequest));
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> generateToken(@RequestBody AuthRequest authRequest) throws JOSEException {
        log.info("Generating tokens for user: {}", authRequest.getUsername());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());
        authenticationManager.authenticate(authenticationToken);

        Map<String, Object> accessToken = authService.getToken(authRequest.getUsername());

        return ResponseEntity.ok(accessToken);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<Map<String, Object>> generateRefreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest)
            throws ParseException, JOSEException {
        log.info("Refreshing token");

        Map<String, Object> refreshToken = authService.getRefreshToken(refreshTokenRequest.getRefreshToken());

        return ResponseEntity.ok(refreshToken);
    }
}
