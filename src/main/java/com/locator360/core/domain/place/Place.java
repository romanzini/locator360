package com.locator360.core.domain.place;

import java.time.Instant;
import java.util.UUID;

public class Place {

  private final UUID id;
  private final UUID circleId;
  private String name;
  private PlaceType type;
  private String addressText;
  private double latitude;
  private double longitude;
  private double radiusMeters;
  private boolean active;
  private final UUID createdByUserId;
  private final Instant createdAt;
  private Instant updatedAt;

  private Place(UUID id, UUID circleId, String name, PlaceType type, String addressText,
      double latitude, double longitude, double radiusMeters, boolean active,
      UUID createdByUserId, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.circleId = circleId;
    this.name = name;
    this.type = type;
    this.addressText = addressText;
    this.latitude = latitude;
    this.longitude = longitude;
    this.radiusMeters = radiusMeters;
    this.active = active;
    this.createdByUserId = createdByUserId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // ─── Factory: criação ───────────────────────────────────────────

  public static Place create(UUID circleId, String name, PlaceType type, String addressText,
      double latitude, double longitude, double radiusMeters,
      UUID createdByUserId) {
    validateCreation(circleId, name, latitude, longitude, radiusMeters, createdByUserId);
    Instant now = Instant.now();
    return new Place(UUID.randomUUID(), circleId, name.trim(), type != null ? type : PlaceType.OTHER,
        addressText, latitude, longitude, radiusMeters, true, createdByUserId, now, now);
  }

  // ─── Factory: reconstituição ────────────────────────────────────

  public static Place restore(UUID id, UUID circleId, String name, PlaceType type, String addressText,
      double latitude, double longitude, double radiusMeters, boolean active,
      UUID createdByUserId, Instant createdAt, Instant updatedAt) {
    return new Place(id, circleId, name, type, addressText, latitude, longitude,
        radiusMeters, active, createdByUserId, createdAt, updatedAt);
  }

  // ─── Business methods ───────────────────────────────────────────

  public void update(String name, PlaceType type, String addressText,
      Double latitude, Double longitude, Double radiusMeters) {
    if (name != null) {
      if (name.isBlank()) {
        throw new IllegalArgumentException("Place name cannot be blank");
      }
      this.name = name.trim();
    }
    if (type != null) {
      this.type = type;
    }
    if (addressText != null) {
      this.addressText = addressText;
    }
    if (latitude != null) {
      validateLatitude(latitude);
      this.latitude = latitude;
    }
    if (longitude != null) {
      validateLongitude(longitude);
      this.longitude = longitude;
    }
    if (radiusMeters != null) {
      validateRadius(radiusMeters);
      this.radiusMeters = radiusMeters;
    }
    this.updatedAt = Instant.now();
  }

  public void deactivate() {
    if (!this.active) {
      throw new IllegalStateException("Place is already inactive");
    }
    this.active = false;
    this.updatedAt = Instant.now();
  }

  // ─── Validations ────────────────────────────────────────────────

  private static void validateCreation(UUID circleId, String name, double latitude,
      double longitude, double radiusMeters, UUID createdByUserId) {
    if (circleId == null) {
      throw new IllegalArgumentException("Circle ID is required");
    }
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Place name is required");
    }
    if (createdByUserId == null) {
      throw new IllegalArgumentException("Creator user ID is required");
    }
    validateLatitude(latitude);
    validateLongitude(longitude);
    validateRadius(radiusMeters);
  }

  private static void validateLatitude(double latitude) {
    if (latitude < -90 || latitude > 90) {
      throw new IllegalArgumentException("Latitude must be between -90 and 90");
    }
  }

  private static void validateLongitude(double longitude) {
    if (longitude < -180 || longitude > 180) {
      throw new IllegalArgumentException("Longitude must be between -180 and 180");
    }
  }

  private static void validateRadius(double radiusMeters) {
    if (radiusMeters <= 0) {
      throw new IllegalArgumentException("Radius must be greater than 0");
    }
  }

  // ─── Getters ────────────────────────────────────────────────────

  public UUID getId() {
    return id;
  }

  public UUID getCircleId() {
    return circleId;
  }

  public String getName() {
    return name;
  }

  public PlaceType getType() {
    return type;
  }

  public String getAddressText() {
    return addressText;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getRadiusMeters() {
    return radiusMeters;
  }

  public boolean isActive() {
    return active;
  }

  public UUID getCreatedByUserId() {
    return createdByUserId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
