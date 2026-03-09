package com.locator360.core.domain.service;

import com.locator360.core.domain.user.*;
import com.locator360.core.port.out.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock
  private PasswordEncoder passwordEncoder;

  private AuthenticationService authenticationService;

  @BeforeEach
  void setUp() {
    authenticationService = new AuthenticationService();
  }

  // ─── validateAccountStatus ────────────────────────────────────

  @Nested
  @DisplayName("validateAccountStatus")
  class ValidateAccountStatusTests {

    @Test
    @DisplayName("should pass for active user")
    void shouldPassForActiveUser() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");
      user.activate();

      assertDoesNotThrow(() -> authenticationService.validateAccountStatus(user));
    }

    @Test
    @DisplayName("should pass for pending verification user")
    void shouldPassForPendingVerificationUser() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");

      assertDoesNotThrow(() -> authenticationService.validateAccountStatus(user));
    }

    @Test
    @DisplayName("should throw for blocked user")
    void shouldThrowForBlockedUser() {
      User user = User.create("maria@example.com", null, "Maria Oliveira");
      user.activate();
      user.block();

      IllegalStateException ex = assertThrows(IllegalStateException.class,
          () -> authenticationService.validateAccountStatus(user));

      assertTrue(ex.getMessage().toLowerCase().contains("blocked"));
    }
  }

  // ─── validatePasswordCredentials ──────────────────────────────

  @Nested
  @DisplayName("validatePasswordCredentials")
  class ValidatePasswordCredentialsTests {

    @Test
    @DisplayName("should pass with valid password")
    void shouldPassWithValidPassword() {
      AuthIdentity identity = AuthIdentity.createPassword(
          UUID.randomUUID(), "maria@example.com", "hashed_password");

      when(passwordEncoder.matches("SenhaForte123!", "hashed_password")).thenReturn(true);

      assertDoesNotThrow(
          () -> authenticationService.validatePasswordCredentials(identity, "SenhaForte123!", passwordEncoder));
    }

    @Test
    @DisplayName("should throw when password does not match")
    void shouldThrowWhenPasswordDoesNotMatch() {
      AuthIdentity identity = AuthIdentity.createPassword(
          UUID.randomUUID(), "maria@example.com", "hashed_password");

      when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> authenticationService.validatePasswordCredentials(identity, "wrong_password", passwordEncoder));

      assertTrue(ex.getMessage().toLowerCase().contains("invalid credentials"));
    }

    @Test
    @DisplayName("should throw when identity is not PASSWORD provider")
    void shouldThrowWhenIdentityIsNotPasswordProvider() {
      AuthIdentity identity = AuthIdentity.createPhoneSms(UUID.randomUUID(), "+5511999999999");

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> authenticationService.validatePasswordCredentials(identity, "password", passwordEncoder));

      assertTrue(ex.getMessage().toLowerCase().contains("invalid provider"));
    }
  }

  // ─── validatePhoneCredentials ─────────────────────────────────

  @Nested
  @DisplayName("validatePhoneCredentials")
  class ValidatePhoneCredentialsTests {

    @Test
    @DisplayName("should pass for verified phone identity")
    void shouldPassForVerifiedPhoneIdentity() {
      AuthIdentity identity = AuthIdentity.createPhoneSms(UUID.randomUUID(), "+5511999999999");

      assertDoesNotThrow(() -> authenticationService.validatePhoneCredentials(identity));
    }

    @Test
    @DisplayName("should throw when identity is not PHONE_SMS provider")
    void shouldThrowWhenIdentityIsNotPhoneSmsProvider() {
      AuthIdentity identity = AuthIdentity.createPassword(
          UUID.randomUUID(), "maria@example.com", "hashed_password");

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> authenticationService.validatePhoneCredentials(identity));

      assertTrue(ex.getMessage().toLowerCase().contains("invalid provider"));
    }

    @Test
    @DisplayName("should throw when phone identity is not verified")
    void shouldThrowWhenPhoneIdentityIsNotVerified() {
      AuthIdentity identity = AuthIdentity.restore(
          UUID.randomUUID(), UUID.randomUUID(), AuthProvider.PHONE_SMS,
          null, null, "+5511999999999", null, false, null,
          Instant.now(), Instant.now());

      IllegalStateException ex = assertThrows(IllegalStateException.class,
          () -> authenticationService.validatePhoneCredentials(identity));

      assertTrue(ex.getMessage().toLowerCase().contains("not verified"));
    }
  }
}
