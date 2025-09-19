package com.hospital.security.configs;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class TokenJwtConfig {
    public static String PREFIX_TOKEN = "Bearer ";
    public static String HEADER_AUTHORIZATION = "Authorization";
    public static String CONTENT_TYPE = "application/json";

    private final SecretKey secretKey;

    public TokenJwtConfig(@Value("${SECRET_KEY}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}
