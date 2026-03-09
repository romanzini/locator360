package com.locator360.infrastructure.security;

import com.locator360.core.port.out.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider implements TokenProvider {

  private final SecretKey key;
  private final long accessTokenExpirationMs;
  private final long refreshTokenExpirationMs;
  private final long accessTokenExpirationSeconds;

  public JwtTokenProvider(JwtProperties properties) {
    this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpirationMs = properties.getAccessTokenExpiration() * 1000L;
    this.refreshTokenExpirationMs = properties.getRefreshTokenExpiration() * 1000L;
    this.accessTokenExpirationSeconds = properties.getAccessTokenExpiration();
  }

  @Override
  public String generateAccessToken(UUID userId) {
    log.debug("Generating access token for user: {}", userId);
    return buildToken(userId, "access", accessTokenExpirationMs);
  }

  @Override
  public String generateRefreshToken(UUID userId) {
    log.debug("Generating refresh token for user: {}", userId);
    return buildToken(userId, "refresh", refreshTokenExpirationMs);
  }

  @Override
  public UUID validateToken(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token)
          .getPayload();
      return UUID.fromString(claims.getSubject());
    } catch (JwtException | IllegalArgumentException ex) {
      log.debug("Invalid JWT token: {}", ex.getMessage());
      throw new IllegalArgumentException("Invalid or expired token");
    }
  }

  @Override
  public long getAccessTokenExpirationSeconds() {
    return accessTokenExpirationSeconds;
  }

  private String buildToken(UUID userId, String type, long expirationMs) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
        .subject(userId.toString())
        .claim("type", type)
        .issuedAt(now)
        .expiration(expiration)
        .signWith(key)
        .compact();
  }
}
