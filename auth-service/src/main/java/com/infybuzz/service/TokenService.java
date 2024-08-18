package com.infybuzz.service;

import com.infybuzz.utils.AuthUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TokenService {

    Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${spring.security.oauth2.authorization-server.jwt.issuer-uri}")
    private String jwtIssuerUri;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private JWKSet jwkSet;

    private static final long ACCESS_TOKEN_EXPIRATION = 300; // 5 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 86400; // 24 hours

    public Map<String, Object> getJwkSet() {
        return jwkSet.toJSONObject();
    }

    public Map<String, String> generateAccessAndRefreshTokens(String username, List<SimpleGrantedAuthority> authorities)
            throws JOSEException {

        List<String> roles = AuthUtils.extractRoles(authorities);
        String accessToken = generateJwtToken(username, roles, ACCESS_TOKEN_EXPIRATION);
        String refreshToken = generateJwtToken(username, roles, REFRESH_TOKEN_EXPIRATION);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    public Map<String, String> refreshToken(String refreshTokenString) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(refreshTokenString);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        String username = claimsSet.getSubject();
        Date expirationTime = claimsSet.getExpirationTime();

        // Validate the token
        if (isTokenExpired(expirationTime)) {
            throw new RuntimeException("Refresh token has expired. Please log in again.");
        }

        // Verify the token
        String keyId = signedJWT.getHeader().getKeyID();
        JWK jwk = jwkSet.getKeyByKeyId(keyId);
        if (jwk == null) {
            throw new RuntimeException("Invalid key ID");
        }

        RSAPublicKey publicKey = (RSAPublicKey) jwk.toRSAKey().toPublicKey();
        RSASSAVerifier verifier = new RSASSAVerifier(publicKey);
        if (!signedJWT.verify(verifier)) {
            throw new RuntimeException("Invalid refresh token");
        }

        List<String> roles = claimsSet.getStringListClaim("roles");
        String newRefreshToken = generateJwtToken(username, roles, REFRESH_TOKEN_EXPIRATION);

        return Map.of("refreshToken", newRefreshToken);
    }

    private boolean isTokenExpired(Date expirationTime) {
        return expirationTime.before(Date.from(Instant.now()));
    }

    private String generateJwtToken(String subject, List<String> roles, long expirationTime) throws JOSEException {

        Date now = Date.from(Instant.now());
        Date expiration = Date.from(Instant.now().plusSeconds(expirationTime));

        // Define JWT Claims
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .notBeforeTime(now)
                .expirationTime(expiration)
                .issueTime(now)
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
        return signedJWT.serialize();
    }

}
