package com.school.erp.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.entity.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final byte[] secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final ObjectMapper objectMapper;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs,
            ObjectMapper objectMapper
    ) {
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.objectMapper = objectMapper;
    }

    public String generateAccessToken(Long userId, Long schoolId, UserRole role) {
        return buildToken(Map.of(
                "userId", userId,
                "schoolId", schoolId,
                "role", role.name(),
                "tokenType", "ACCESS"
        ), accessTokenExpirationMs);
    }

    public String generateRefreshToken(Long userId, Long schoolId, UserRole role) {
        return buildToken(Map.of(
                "userId", userId,
                "schoolId", schoolId,
                "role", role.name(),
                "tokenType", "REFRESH"
        ), refreshTokenExpirationMs);
    }

    public AuthenticatedUser parseAccessToken(String token) {
        Map<String, Object> claims = parseClaims(token);
        String tokenType = (String) claims.get("tokenType");
        if (!"ACCESS".equals(tokenType)) {
            throw new IllegalArgumentException("Invalid access token");
        }
        return new AuthenticatedUser(
                ((Number) claims.get("userId")).longValue(),
                ((Number) claims.get("schoolId")).longValue(),
                UserRole.valueOf((String) claims.get("role"))
        );
    }

    public boolean isValid(String token) {
        parseClaims(token);
        return true;
    }

    public boolean isValidRefreshToken(String token) {
        try {
            Map<String, Object> claims = parseClaims(token);
            return "REFRESH".equals(claims.get("tokenType"));
        } catch (Exception e) {
            return false;
        }
    }

    private String buildToken(Map<String, Object> claims, long expirationMs) {
        try {
            Instant now = Instant.now();
            String header = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            )));

            Map<String, Object> payloadClaims = new LinkedHashMap<>(claims);
            payloadClaims.put("iat", now.getEpochSecond());
            payloadClaims.put("exp", now.plusMillis(expirationMs).getEpochSecond());
            String payload = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(payloadClaims));
            String signature = sign(header + "." + payload);
            return header + "." + payload + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate token", exception);
        }
    }

    private Map<String, Object> parseClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token");
            }

            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!expectedSignature.equals(parts[2])) {
                throw new IllegalArgumentException("Invalid token signature");
            }

            Map<String, Object> claims = objectMapper.readValue(
                    URL_DECODER.decode(parts[1]),
                    new TypeReference<>() {
                    }
            );
            long expirationEpochSecond = ((Number) claims.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= expirationEpochSecond) {
                throw new IllegalArgumentException("Token expired");
            }
            return claims;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid token", exception);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign token", exception);
        }
    }
}
