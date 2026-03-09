package com.familylocator.core.domain.user;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class VerificationToken {

  private static final long DEFAULT_EXPIRATION_HOURS = 24;

  private final UUID id;
  private final UUID userId;
  private final TokenType type;
  private final String token;
  private final Instant expiresAt;
  private Instant usedAt;
  private final Instant createdAt;

  private VerificationToken(UUID id, UUID userId, TokenType type, String token,
      Instant expiresAt, Instant usedAt, Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.type = type;
    this.token = token;
    this.expiresAt = expiresAt;
    this.usedAt = usedAt;
    this.createdAt = createdAt;
  }

  // ─── Factory: criação ───────────────────────────────────────────

  public static VerificationToken create(UUID userId, TokenType type) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID is required");
    }
    if (type == null) {
      throw new IllegalArgumentException("Token type is required");
    }

    Instant now = Instant.now();
    return new VerificationToken(
        UUID.randomUUID(),
        userId,
        type,
        UUID.randomUUID().toString(),
        now.plus(DEFAULT_EXPIRATION_HOURS, ChronoUnit.HOURS),
        null,
        now);
  }

  // ─── Factory: reconstituição ────────────────────────────────────

  public static VerificationToken restore(UUID id, UUID userId, TokenType type,
      String token, Instant expiresAt,
      Instant usedAt, Instant createdAt) {
    return new VerificationToken(id, userId, type, token, expiresAt, usedAt, createdAt);
  }

  // ─── Business methods ───────────────────────────────────────────

  public void markUsed() {
    if (this.usedAt != null) {
      throw new IllegalStateException("Token has already been used");
    }
    this.usedAt = Instant.now();
  }

  public boolean isExpired() {
    return Instant.now().isAfter(this.expiresAt);
  }

  public boolean isValid() {
    return this.usedAt == null && !isExpired();
  }

  // ─── Getters ────────────────────────────────────────────────────

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public TokenType getType() {
    return type;
  }

  public String getToken() {
    return token;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getUsedAt() {
    return usedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
