package com.locator360.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp() {
    JwtProperties properties = new JwtProperties();
    properties.setSecret("my-super-secret-key-for-testing-must-be-at-least-32-bytes-long!");
    properties.setAccessTokenExpiration(3600);
    properties.setRefreshTokenExpiration(604800);
    jwtTokenProvider = new JwtTokenProvider(properties);
  }

  @Nested
  @DisplayName("generateAccessToken")
  class GenerateAccessTokenTests {

    @Test
    @DisplayName("should generate a non-null access token")
    void shouldGenerateToken() {
      UUID userId = UUID.randomUUID();
      String token = jwtTokenProvider.generateAccessToken(userId);
      assertNotNull(token);
      assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("should generate different tokens for different users")
    void shouldGenerateDifferentTokens() {
      String token1 = jwtTokenProvider.generateAccessToken(UUID.randomUUID());
      String token2 = jwtTokenProvider.generateAccessToken(UUID.randomUUID());
      assertNotEquals(token1, token2);
    }
  }

  @Nested
  @DisplayName("generateRefreshToken")
  class GenerateRefreshTokenTests {

    @Test
    @DisplayName("should generate a non-null refresh token")
    void shouldGenerateRefreshToken() {
      UUID userId = UUID.randomUUID();
      String token = jwtTokenProvider.generateRefreshToken(userId);
      assertNotNull(token);
      assertFalse(token.isBlank());
    }
  }

  @Nested
  @DisplayName("validateToken")
  class ValidateTokenTests {

    @Test
    @DisplayName("should return userId from valid access token")
    void shouldValidateAccessToken() {
      UUID userId = UUID.randomUUID();
      String token = jwtTokenProvider.generateAccessToken(userId);

      UUID result = jwtTokenProvider.validateToken(token);

      assertEquals(userId, result);
    }

    @Test
    @DisplayName("should return userId from valid refresh token")
    void shouldValidateRefreshToken() {
      UUID userId = UUID.randomUUID();
      String token = jwtTokenProvider.generateRefreshToken(userId);

      UUID result = jwtTokenProvider.validateToken(token);

      assertEquals(userId, result);
    }

    @Test
    @DisplayName("should throw for invalid token")
    void shouldThrowForInvalidToken() {
      assertThrows(IllegalArgumentException.class,
          () -> jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    @DisplayName("should throw for tampered token")
    void shouldThrowForTamperedToken() {
      UUID userId = UUID.randomUUID();
      String token = jwtTokenProvider.generateAccessToken(userId);
      String tampered = token.substring(0, token.length() - 5) + "XXXXX";

      assertThrows(IllegalArgumentException.class,
          () -> jwtTokenProvider.validateToken(tampered));
    }
  }

  @Nested
  @DisplayName("getAccessTokenExpirationSeconds")
  class GetExpirationTests {

    @Test
    @DisplayName("should return configured expiration")
    void shouldReturnExpiration() {
      assertEquals(3600L, jwtTokenProvider.getAccessTokenExpirationSeconds());
    }
  }
}
