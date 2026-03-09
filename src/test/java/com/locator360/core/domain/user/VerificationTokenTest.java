package com.locator360.core.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VerificationTokenTest {

  // ─── Factory create() ───────────────────────────────────────────

  @Nested
  @DisplayName("VerificationToken.create()")
  class CreateTests {

    @Test
    @DisplayName("should create email verification token")
    void shouldCreateEmailVerificationToken() {
      UUID userId = UUID.randomUUID();

      VerificationToken token = VerificationToken.create(userId, TokenType.EMAIL_VERIFICATION);

      assertNotNull(token.getId());
      assertEquals(userId, token.getUserId());
      assertEquals(TokenType.EMAIL_VERIFICATION, token.getType());
      assertNotNull(token.getToken());
      assertFalse(token.getToken().isEmpty());
      assertNotNull(token.getExpiresAt());
      assertTrue(token.getExpiresAt().isAfter(Instant.now()));
      assertNull(token.getUsedAt());
      assertNotNull(token.getCreatedAt());
    }

    @Test
    @DisplayName("should create phone verification token")
    void shouldCreatePhoneVerificationToken() {
      UUID userId = UUID.randomUUID();

      VerificationToken token = VerificationToken.create(userId, TokenType.PHONE_VERIFICATION);

      assertEquals(TokenType.PHONE_VERIFICATION, token.getType());
    }

    @Test
    @DisplayName("should create password reset token")
    void shouldCreatePasswordResetToken() {
      UUID userId = UUID.randomUUID();

      VerificationToken token = VerificationToken.create(userId, TokenType.PASSWORD_RESET);

      assertEquals(TokenType.PASSWORD_RESET, token.getType());
    }

    @Test
    @DisplayName("should throw when userId is null")
    void shouldThrowWhenUserIdIsNull() {
      assertThrows(IllegalArgumentException.class,
          () -> VerificationToken.create(null, TokenType.EMAIL_VERIFICATION));
    }

    @Test
    @DisplayName("should throw when token type is null")
    void shouldThrowWhenTokenTypeIsNull() {
      assertThrows(IllegalArgumentException.class,
          () -> VerificationToken.create(UUID.randomUUID(), null));
    }
  }

  // ─── Business methods ───────────────────────────────────────────

  @Nested
  @DisplayName("Business methods")
  class BusinessMethodTests {

    @Test
    @DisplayName("should mark token as used")
    void shouldMarkTokenAsUsed() {
      VerificationToken token = VerificationToken.create(UUID.randomUUID(), TokenType.EMAIL_VERIFICATION);

      token.markUsed();

      assertNotNull(token.getUsedAt());
    }

    @Test
    @DisplayName("should throw when marking an already used token")
    void shouldThrowWhenMarkingAlreadyUsedToken() {
      VerificationToken token = VerificationToken.create(UUID.randomUUID(), TokenType.EMAIL_VERIFICATION);
      token.markUsed();

      assertThrows(IllegalStateException.class, token::markUsed);
    }

    @Test
    @DisplayName("should identify token as not expired when within validity period")
    void shouldNotBeExpired() {
      VerificationToken token = VerificationToken.create(UUID.randomUUID(), TokenType.EMAIL_VERIFICATION);

      assertFalse(token.isExpired());
    }

    @Test
    @DisplayName("should identify token as expired when past expiration")
    void shouldBeExpired() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Instant pastExpiry = Instant.now().minusSeconds(3600);

      VerificationToken token = VerificationToken.restore(id, userId,
          TokenType.EMAIL_VERIFICATION, "tok123", pastExpiry, null, Instant.now());

      assertTrue(token.isExpired());
    }

    @Test
    @DisplayName("should identify used token as invalid")
    void shouldBeInvalidWhenUsed() {
      VerificationToken token = VerificationToken.create(UUID.randomUUID(), TokenType.EMAIL_VERIFICATION);
      token.markUsed();

      assertFalse(token.isValid());
    }

    @Test
    @DisplayName("should identify valid token")
    void shouldBeValid() {
      VerificationToken token = VerificationToken.create(UUID.randomUUID(), TokenType.EMAIL_VERIFICATION);

      assertTrue(token.isValid());
    }
  }

  // ─── Factory restore() ──────────────────────────────────────────

  @Nested
  @DisplayName("VerificationToken.restore()")
  class RestoreTests {

    @Test
    @DisplayName("should restore token with all fields")
    void shouldRestoreToken() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Instant now = Instant.now();
      Instant expires = now.plusSeconds(3600);

      VerificationToken token = VerificationToken.restore(id, userId,
          TokenType.EMAIL_VERIFICATION, "abc-123", expires, null, now);

      assertEquals(id, token.getId());
      assertEquals(userId, token.getUserId());
      assertEquals(TokenType.EMAIL_VERIFICATION, token.getType());
      assertEquals("abc-123", token.getToken());
      assertEquals(expires, token.getExpiresAt());
      assertNull(token.getUsedAt());
      assertEquals(now, token.getCreatedAt());
    }
  }
}
