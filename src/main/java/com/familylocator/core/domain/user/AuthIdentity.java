package com.familylocator.core.domain.user;

import java.time.Instant;
import java.util.UUID;

public class AuthIdentity {

  private final UUID id;
  private final UUID userId;
  private final AuthProvider provider;
  private final String providerUserId;
  private String email;
  private String phoneNumber;
  private String passwordHash;
  private boolean verified;
  private Instant lastLoginAt;
  private Instant createdAt;
  private Instant updatedAt;

  private AuthIdentity(UUID id, UUID userId, AuthProvider provider, String providerUserId,
      String email, String phoneNumber, String passwordHash,
      boolean verified, Instant lastLoginAt, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.provider = provider;
    this.providerUserId = providerUserId;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.passwordHash = passwordHash;
    this.verified = verified;
    this.lastLoginAt = lastLoginAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // ─── Factory: password identity ─────────────────────────────────

  public static AuthIdentity createPassword(UUID userId, String email, String passwordHash) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID is required");
    }
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email is required for password provider");
    }
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new IllegalArgumentException("Password hash is required for password provider");
    }

    Instant now = Instant.now();
    return new AuthIdentity(UUID.randomUUID(), userId, AuthProvider.PASSWORD,
        null, email, null, passwordHash, false, null, now, now);
  }

  // ─── Factory: social identity ───────────────────────────────────

  public static AuthIdentity createSocial(UUID userId, AuthProvider provider,
      String providerUserId, String email) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID is required");
    }
    if (providerUserId == null || providerUserId.isBlank()) {
      throw new IllegalArgumentException("Provider user ID is required for social provider");
    }

    Instant now = Instant.now();
    return new AuthIdentity(UUID.randomUUID(), userId, provider,
        providerUserId, email, null, null, true, null, now, now);
  }

  // ─── Factory: phone SMS identity ────────────────────────────────

  public static AuthIdentity createPhoneSms(UUID userId, String phoneNumber) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID is required");
    }
    if (phoneNumber == null || phoneNumber.isBlank()) {
      throw new IllegalArgumentException("Phone number is required for phone SMS provider");
    }

    Instant now = Instant.now();
    return new AuthIdentity(UUID.randomUUID(), userId, AuthProvider.PHONE_SMS,
        null, null, phoneNumber, null, true, null, now, now);
  }

  // ─── Factory: reconstituição ────────────────────────────────────

  public static AuthIdentity restore(UUID id, UUID userId, AuthProvider provider,
      String providerUserId, String email, String phoneNumber,
      String passwordHash, boolean verified, Instant lastLoginAt,
      Instant createdAt, Instant updatedAt) {
    return new AuthIdentity(id, userId, provider, providerUserId, email,
        phoneNumber, passwordHash, verified, lastLoginAt, createdAt, updatedAt);
  }

  // ─── Business methods ───────────────────────────────────────────

  public void markVerified() {
    this.verified = true;
    this.updatedAt = Instant.now();
  }

  public void recordLogin() {
    this.lastLoginAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  // ─── Getters ────────────────────────────────────────────────────

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public AuthProvider getProvider() {
    return provider;
  }

  public String getProviderUserId() {
    return providerUserId;
  }

  public String getEmail() {
    return email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public boolean isVerified() {
    return verified;
  }

  public Instant getLastLoginAt() {
    return lastLoginAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
