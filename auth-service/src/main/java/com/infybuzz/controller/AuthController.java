package com.infybuzz.controller;

import com.infybuzz.entity.UserCredEntity;
import com.infybuzz.repository.UserCredRepository;
import com.infybuzz.request.AuthRequest;
import com.infybuzz.security.CustomUserDetailsService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${spring.security.oauth2.authorization-server.jwt.issuer-uri}")
    private String jwtIssuerUri;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private JWKSet jwkSet;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    UserCredRepository repository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public UserCredEntity addNewUser(@RequestBody UserCredEntity user) {
        logger.info("Inside addNewUser: " + user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        return repository.save(user);
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwkSet() {
        logger.info("Inside getJwkSet");

        return jwkSet.toJSONObject();
    }

    @PostMapping("/token")
    public Map<String, String> getToken(@RequestBody AuthRequest authRequest) throws JOSEException {
        logger.info("Inside getToken = " + authRequest);

        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        logger.info("userDetails = " + userDetails);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(),
                authRequest.getPassword(),
                userDetails.getAuthorities());
        logger.info("authenticationToken = " + authenticationToken);

        if (authenticationToken.isAuthenticated()) {
            return generateJwtToken(authenticationToken);
        }

      return Map.of();
    }

    private Map<String, String> generateJwtToken(UsernamePasswordAuthenticationToken authenticationToken)
            throws JOSEException {

        // Extract scopes
        List<String> roles = authenticationToken.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("ROLE_", ""))
                .toList();
        logger.info("roles list: " + roles);

        // Define JWT Claims
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(authenticationToken.getName())
                .notBeforeTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plusSeconds(1800))) // 30 minutes expiry
                .issueTime(Date.from(Instant.now()))
                .issuer(jwtIssuerUri)
                .claim("roles", roles)
                .build();

        // Create JWS header with the key ID
        JWK jwk = jwkSet.getKeys().get(0);
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(jwk.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

        // Create the signed JWT
        SignedJWT signedJWT = new SignedJWT(header, claims);
        RSASSASigner signer = new RSASSASigner(keyPair.getPrivate());
        signedJWT.sign(signer);

        // Serialize the JWT to a compact form
        return Map.of("accessToken", signedJWT.serialize());
    }

}