package com.locator360.core.domain.user;

import java.time.Instant;
import java.util.UUID;

public class Device {

  private final UUID id;
  private final UUID userId;
  private final Platform platform;
  private String deviceModel;
  private String osVersion;
  private String appVersion;
  private String pushToken;
  private boolean active;
  private Instant lastSeenAt;
  private Instant createdAt;
  private Instant updatedAt;

  private Device(UUID id, UUID userId, Platform platform, String deviceModel,
      String osVersion, String appVersion, String pushToken,
      boolean active, Instant lastSeenAt, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.platform = platform;
    this.deviceModel = deviceModel;
    this.osVersion = osVersion;
    this.appVersion = appVersion;
    this.pushToken = pushToken;
    this.active = active;
    this.lastSeenAt = lastSeenAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // ─── Factory: criação ───────────────────────────────────────────

  public static Device create(UUID userId, Platform platform, String deviceModel,
      String osVersion, String appVersion, String pushToken) {
    validateCreation(userId, platform);

    Instant now = Instant.now();
    return new Device(
        UUID.randomUUID(),
        userId,
        platform,
        deviceModel,
        osVersion,
        appVersion,
        pushToken,
        true,
        now,
        now,
        now);
  }

  // ─── Factory: reconstituição ────────────────────────────────────

  public static Device restore(UUID id, UUID userId, Platform platform, String deviceModel,
      String osVersion, String appVersion, String pushToken,
      boolean active, Instant lastSeenAt, Instant createdAt, Instant updatedAt) {
    return new Device(id, userId, platform, deviceModel, osVersion, appVersion,
        pushToken, active, lastSeenAt, createdAt, updatedAt);
  }

  // ─── Business methods ───────────────────────────────────────────

  public void activate() {
    this.active = true;
    this.updatedAt = Instant.now();
  }

  public void deactivate() {
    this.active = false;
    this.updatedAt = Instant.now();
  }

  public void updateLastSeen() {
    this.lastSeenAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public void updatePushToken(String pushToken) {
    this.pushToken = pushToken;
    this.updatedAt = Instant.now();
  }

  public void updateAppVersion(String appVersion) {
    this.appVersion = appVersion;
    this.updatedAt = Instant.now();
  }

  // ─── Validations ────────────────────────────────────────────────

  private static void validateCreation(UUID userId, Platform platform) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID is required");
    }
    if (platform == null) {
      throw new IllegalArgumentException("Platform is required");
    }
  }

  // ─── Getters ────────────────────────────────────────────────────

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public Platform getPlatform() {
    return platform;
  }

  public String getDeviceModel() {
    return deviceModel;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public String getPushToken() {
    return pushToken;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getLastSeenAt() {
    return lastSeenAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
