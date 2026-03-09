package com.familylocator.infrastructure.persistence.postgresql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_identities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthIdentityJpaEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private String provider;

  @Column(name = "provider_user_id")
  private String providerUserId;

  private String email;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "is_verified", nullable = false)
  private boolean verified;

  @Column(name = "last_login_at")
  private Instant lastLoginAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
