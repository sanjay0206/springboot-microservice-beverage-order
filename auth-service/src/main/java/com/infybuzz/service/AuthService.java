package com.infybuzz.service;

import com.infybuzz.config.AppConfig;
import com.infybuzz.entity.Role;
import com.infybuzz.entity.UserEntity;
import com.infybuzz.exceptions.AuthAPIException;
import com.infybuzz.repository.RoleRepository;
import com.infybuzz.repository.UserRepository;
import com.infybuzz.request.UserRequest;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class AuthService {

    private final AppConfig appConfig;
    private final KeyPair keyPair;
    private final JWKSet jwkSet;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthService(AppConfig appConfig,
                       KeyPair keyPair,
                       JWKSet jwkSet,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       UserDetailsService userDetailsService) {
        this.appConfig = appConfig;
        this.keyPair = keyPair;
        this.jwkSet = jwkSet;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    public Map<String, Object> getJwkSet() {
        return jwkSet.toJSONObject();
    }

    private boolean isTokenExpired(Date expirationTime) {
        return expirationTime.before(Date.from(Instant.now()));
    }

    private String generateJwtToken(String username, List<String> roles, long expirationTime) throws JOSEException {
        Date now = Date.from(Instant.now());
        Date expiration = Date.from(Instant.now().plusSeconds(expirationTime));

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(username)
                .notBeforeTime(now)
                .expirationTime(expiration)
                .issueTime(now)
                .claim("roles", roles)
                .issuer(appConfig.getJwtIssuerUri());

        // Create JWS header with the key ID
        JWK jwk = jwkSet.getKeys()
                .stream()
                .findFirst()
                .orElseThrow(() -> new AuthAPIException(HttpStatus.NOT_FOUND, "No JWK key available"));

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(jwk.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

        // Create the signed JWT
        SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());
        RSASSASigner signer = new RSASSASigner(keyPair.getPrivate());
        signedJWT.sign(signer);

        // Serialize the JWT to a compact form
        return signedJWT.serialize();
    }

    public Map<String, Object> getToken(String username) throws JOSEException {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = generateJwtToken(username, roles, appConfig.getAccessTokenExpiration());
        String refreshToken = generateJwtToken(username, roles, appConfig.getRefreshTokenExpiration());

        return Map.of(
                "access_token", accessToken,
                "refresh_token", refreshToken,
                "expires_in", appConfig.getAccessTokenExpiration(),
                "refresh_expires_in", appConfig.getRefreshTokenExpiration(),
                "token_type", "Bearer"
        );
    }

    public Map<String, Object> getRefreshToken(String refreshToken) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(refreshToken);

        // Verify the signature of the refresh token
        JWK jwk = jwkSet.getKeys()
                .stream()
                .findFirst()
                .orElseThrow(() -> new AuthAPIException(HttpStatus.NOT_FOUND, "No JWK key available"));
        RSAPublicKey publicKey = (RSAPublicKey) jwk.toRSAKey().toPublicKey();
        RSASSAVerifier verifier = new RSASSAVerifier(publicKey);
        if (!signedJWT.verify(verifier)) {
            throw new AuthAPIException(HttpStatus.UNAUTHORIZED, "Invalid refresh token signature");
        }

        // Check if the token is expired
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        Date expirationTime = claims.getExpirationTime();
        if (isTokenExpired(expirationTime)) {
            throw new AuthAPIException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        String username = claims.getSubject();
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String newAccessToken = generateJwtToken(username, roles, appConfig.getAccessTokenExpiration());
        String newRefreshToken = generateJwtToken(username, roles, appConfig.getRefreshTokenExpiration());

        return Map.of(
                "access_token", newAccessToken,
                "refresh_token", newRefreshToken,
                "expires_in", appConfig.getAccessTokenExpiration(),
                "refresh_expires_in", appConfig.getRefreshTokenExpiration(),
                "token_type", "Bearer"
        );
    }

    public UserRequest registerUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthAPIException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthAPIException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        List<Role> validatedRoles = roleRepository.findByNameIn(request.getRoles());
        if (validatedRoles.isEmpty()) {
            throw new AuthAPIException(HttpStatus.NOT_FOUND, "Role is not found");
        }

        if (validatedRoles.stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()))) {
            throw new AuthAPIException(HttpStatus.FORBIDDEN, "Can not register as admin");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(validatedRoles);
        user.setCreatedAt(LocalDateTime.now());
        UserEntity savedUser = userRepository.save(user);

        return mapEntityToRequest(savedUser);
    }

    private UserRequest mapEntityToRequest(UserEntity userEntity) {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(userEntity.getUsername());
        userRequest.setEmail(userEntity.getEmail());
        userRequest.setRoles(userEntity.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        return userRequest;
    }
}
