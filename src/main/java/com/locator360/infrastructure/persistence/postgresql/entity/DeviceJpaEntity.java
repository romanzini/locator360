package com.locator360.infrastructure.persistence.postgresql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceJpaEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private String platform;

  @Column(name = "device_model")
  private String deviceModel;

  @Column(name = "os_version")
  private String osVersion;

  @Column(name = "app_version")
  private String appVersion;

  @Column(name = "push_token")
  private String pushToken;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Column(name = "last_seen_at")
  private Instant lastSeenAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
