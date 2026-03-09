package com.locator360.core.domain.service;

import com.locator360.core.domain.user.AuthIdentity;
import com.locator360.core.domain.user.AuthProvider;
import com.locator360.core.domain.user.User;
import com.locator360.core.domain.user.UserStatus;
import com.locator360.core.port.out.PasswordEncoder;

public class AuthenticationService {

  // ─── Account status validation ────────────────────────────────

  public void validateAccountStatus(User user) {
    if (user.getStatus() == UserStatus.BLOCKED) {
      throw new IllegalStateException("Account is blocked");
    }
  }

  // ─── Password credentials validation ─────────────────────────

  public void validatePasswordCredentials(AuthIdentity identity, String rawPassword,
      PasswordEncoder passwordEncoder) {
    if (identity.getProvider() != AuthProvider.PASSWORD) {
      throw new IllegalArgumentException("Invalid provider for password authentication");
    }
    if (identity.getPasswordHash() == null || identity.getPasswordHash().isBlank()) {
      throw new IllegalStateException("Password hash is not set");
    }
    if (!passwordEncoder.matches(rawPassword, identity.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }
  }

  // ─── Phone credentials validation ────────────────────────────

  public void validatePhoneCredentials(AuthIdentity identity) {
    if (identity.getProvider() != AuthProvider.PHONE_SMS) {
      throw new IllegalArgumentException("Invalid provider for phone authentication");
    }
    if (!identity.isVerified()) {
      throw new IllegalStateException("Phone identity is not verified");
    }
  }
}
