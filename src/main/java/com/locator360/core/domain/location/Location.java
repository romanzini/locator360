package com.locator360.core.domain.location;

import java.time.Instant;
import java.util.UUID;

public class Location {

    // Construtor padrão para deserialização do Jackson/Kafka
    protected Location() {
        this.id = null;
        this.userId = null;
        this.circleId = null;
        this.latitude = 0;
        this.longitude = 0;
        this.accuracyMeters = null;
        this.speedMps = null;
        this.headingDegrees = null;
        this.altitudeMeters = null;
        this.source = null;
        this.recordedAt = null;
        this.receivedAt = null;
        this.isMoving = false;
        this.batteryLevel = null;
        this.createdAt = null;
    }

    private final UUID id;
    private final UUID userId;
    private final UUID circleId;
    private final double latitude;
    private final double longitude;
    private final Double accuracyMeters;
    private final Double speedMps;
    private final Double headingDegrees;
    private final Double altitudeMeters;
    private final LocationSource source;
    private final Instant recordedAt;
    private final Instant receivedAt;
    private final boolean isMoving;
    private final Integer batteryLevel;
    private final Instant createdAt;

    private Location(UUID id, UUID userId, UUID circleId, double latitude, double longitude,
                     Double accuracyMeters, Double speedMps, Double headingDegrees,
                     Double altitudeMeters, LocationSource source, Instant recordedAt,
                     Instant receivedAt, boolean isMoving, Integer batteryLevel, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.circleId = circleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracyMeters = accuracyMeters;
        this.speedMps = speedMps;
        this.headingDegrees = headingDegrees;
        this.altitudeMeters = altitudeMeters;
        this.source = source;
        this.recordedAt = recordedAt;
        this.receivedAt = receivedAt;
        this.isMoving = isMoving;
        this.batteryLevel = batteryLevel;
        this.createdAt = createdAt;
    }

    // ─── Factory: criação ───────────────────────────────────────────

    public static Location create(UUID userId, UUID circleId, double latitude, double longitude,
                                  Double accuracyMeters, Double speedMps, Double headingDegrees,
                                  Double altitudeMeters, LocationSource source, Instant recordedAt,
                                  boolean isMoving, Integer batteryLevel) {
        validateCreation(userId, latitude, longitude, source, recordedAt);
        validateBatteryLevel(batteryLevel);

        return new Location(
                UUID.randomUUID(), userId, circleId, latitude, longitude,
                accuracyMeters, speedMps, headingDegrees, altitudeMeters,
                source, recordedAt, Instant.now(), isMoving, batteryLevel, Instant.now());
    }

    // ─── Factory: reconstituição ────────────────────────────────────

    public static Location restore(UUID id, UUID userId, UUID circleId, double latitude, double longitude,
                                   Double accuracyMeters, Double speedMps, Double headingDegrees,
                                   Double altitudeMeters, LocationSource source, Instant recordedAt,
                                   Instant receivedAt, boolean isMoving, Integer batteryLevel,
                                   Instant createdAt) {
        return new Location(id, userId, circleId, latitude, longitude,
                accuracyMeters, speedMps, headingDegrees, altitudeMeters,
                source, recordedAt, receivedAt, isMoving, batteryLevel, createdAt);
    }

    // ─── Validations ────────────────────────────────────────────────

    private static void validateCreation(UUID userId, double latitude, double longitude,
                                         LocationSource source, Instant recordedAt) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        if (source == null) {
            throw new IllegalArgumentException("Location source is required");
        }
        if (recordedAt == null) {
            throw new IllegalArgumentException("Recorded at timestamp is required");
        }
    }

    private static void validateBatteryLevel(Integer batteryLevel) {
        if (batteryLevel != null && (batteryLevel < 0 || batteryLevel > 100)) {
            throw new IllegalArgumentException("Battery level must be between 0 and 100");
        }
    }

    // ─── Getters ────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCircleId() {
        return circleId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Double getAccuracyMeters() {
        return accuracyMeters;
    }

    public Double getSpeedMps() {
        return speedMps;
    }

    public Double getHeadingDegrees() {
        return headingDegrees;
    }

    public Double getAltitudeMeters() {
        return altitudeMeters;
    }

    public LocationSource getSource() {
        return source;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
