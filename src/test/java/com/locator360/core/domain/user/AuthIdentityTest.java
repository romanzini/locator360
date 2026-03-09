package com.locator360.core.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthIdentityTest {

  // ─── Factory create() ───────────────────────────────────────────

  @Nested
  @DisplayName("AuthIdentity.create()")
  class CreateTests {

    @Test
    @DisplayName("should create password identity with hashed password")
    void shouldCreatePasswordIdentity() {
      UUID userId = UUID.randomUUID();

      AuthIdentity identity = AuthIdentity.createPassword(userId, "maria@example.com", "hashed_password");

      assertNotNull(identity.getId());
      assertEquals(userId, identity.getUserId());
      assertEquals(AuthProvider.PASSWORD, identity.getProvider());
      assertEquals("maria@example.com", identity.getEmail());
      assertEquals("hashed_password", identity.getPasswordHash());
      assertFalse(identity.isVerified());
      assertNull(identity.getLastLoginAt());
      assertNotNull(identity.getCreatedAt());
    }

    @Test
    @DisplayName("should create social identity (Google)")
    void shouldCreateGoogleIdentity() {
      UUID userId = UUID.randomUUID();

      AuthIdentity identity = AuthIdentity.createSocial(userId, AuthProvider.GOOGLE,
          "google-uid-123", "maria@example.com");

      assertEquals(AuthProvider.GOOGLE, identity.getProvider());
      assertEquals("google-uid-123", identity.getProviderUserId());
      assertEquals("maria@example.com", identity.getEmail());
      assertNull(identity.getPasswordHash());
      assertTrue(identity.isVerified());
    }

    @Test
    @DisplayName("should create phone SMS identity")
    void shouldCreatePhoneSmsIdentity() {
      UUID userId = UUID.randomUUID();

      AuthIdentity identity = AuthIdentity.createPhoneSms(userId, "+5511999999999");

      assertEquals(AuthProvider.PHONE_SMS, identity.getProvider());
      assertEquals("+5511999999999", identity.getPhoneNumber());
      assertNull(identity.getEmail());
      assertNull(identity.getPasswordHash());
      assertTrue(identity.isVerified());
    }

    @Test
    @DisplayName("should throw when userId is null")
    void shouldThrowWhenUserIdIsNull() {
      assertThrows(IllegalArgumentException.class,
          () -> AuthIdentity.createPassword(null, "maria@example.com", "hash"));
    }

    @Test
    @DisplayName("should throw when email is null for password provider")
    void shouldThrowWhenEmailIsNullForPasswordProvider() {
      assertThrows(IllegalArgumentException.class,
          () -> AuthIdentity.createPassword(UUID.randomUUID(), null, "hash"));
    }

    @Test
    @DisplayName("should throw when password hash is null for password provider")
    void shouldThrowWhenPasswordHashIsNull() {
      assertThrows(IllegalArgumentException.class,
          () -> AuthIdentity.createPassword(UUID.randomUUID(), "maria@example.com", null));
    }

    @Test
    @DisplayName("should throw when provider user id is null for social provider")
    void shouldThrowWhenProviderUserIdIsNull() {
      assertThrows(IllegalArgumentException.class,
          () -> AuthIdentity.createSocial(UUID.randomUUID(), AuthProvider.GOOGLE, null, "email@test.com"));
    }
  }

  // ─── Business methods ───────────────────────────────────────────

  @Nested
  @DisplayName("Business methods")
  class BusinessMethodTests {

    @Test
    @DisplayName("should mark as verified")
    void shouldMarkAsVerified() {
      AuthIdentity identity = AuthIdentity.createPassword(UUID.randomUUID(), "maria@example.com", "hash");

      identity.markVerified();

      assertTrue(identity.isVerified());
    }

    @Test
    @DisplayName("should record login")
    void shouldRecordLogin() {
      AuthIdentity identity = AuthIdentity.createPassword(UUID.randomUUID(), "maria@example.com", "hash");
      identity.markVerified();

      identity.recordLogin();

      assertNotNull(identity.getLastLoginAt());
    }
  }

  // ─── Factory restore() ──────────────────────────────────────────

  @Nested
  @DisplayName("AuthIdentity.restore()")
  class RestoreTests {

    @Test
    @DisplayName("should restore identity with all fields")
    void shouldRestoreIdentity() {
      UUID id = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Instant now = Instant.now();

      AuthIdentity identity = AuthIdentity.restore(id, userId, AuthProvider.PASSWORD,
          "provider-uid", "maria@example.com", "+5511999999999",
          "hashed", true, now, now, now);

      assertEquals(id, identity.getId());
      assertEquals(userId, identity.getUserId());
      assertEquals(AuthProvider.PASSWORD, identity.getProvider());
      assertEquals("provider-uid", identity.getProviderUserId());
      assertEquals("maria@example.com", identity.getEmail());
      assertEquals("+5511999999999", identity.getPhoneNumber());
      assertEquals("hashed", identity.getPasswordHash());
      assertTrue(identity.isVerified());
      assertEquals(now, identity.getLastLoginAt());
    }
  }
}
