package com.locator360.core.domain.location;

import java.time.Instant;
import java.util.UUID;

public class LocationSharingState {

    private final UUID id;
    private final UUID userId;
    private final UUID circleId;
    private boolean isSharingLocation;
    private boolean isHistoryEnabled;
    private Instant pausedUntil;
    private UUID lastKnownLocationId;
    private Instant lastUpdatedAt;

    private LocationSharingState(UUID id, UUID userId, UUID circleId,
                                 boolean isSharingLocation, boolean isHistoryEnabled,
                                 Instant pausedUntil, UUID lastKnownLocationId,
                                 Instant lastUpdatedAt) {
        this.id = id;
        this.userId = userId;
        this.circleId = circleId;
        this.isSharingLocation = isSharingLocation;
        this.isHistoryEnabled = isHistoryEnabled;
        this.pausedUntil = pausedUntil;
        this.lastKnownLocationId = lastKnownLocationId;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    // ─── Factory: criação ───────────────────────────────────────────

    public static LocationSharingState create(UUID userId, UUID circleId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (circleId == null) {
            throw new IllegalArgumentException("Circle ID is required");
        }
        return new LocationSharingState(UUID.randomUUID(), userId, circleId,
                true, true, null, null, Instant.now());
    }

    // ─── Factory: reconstituição ────────────────────────────────────

    public static LocationSharingState restore(UUID id, UUID userId, UUID circleId,
                                                boolean isSharingLocation, boolean isHistoryEnabled,
                                                Instant pausedUntil, UUID lastKnownLocationId,
                                                Instant lastUpdatedAt) {
        return new LocationSharingState(id, userId, circleId,
                isSharingLocation, isHistoryEnabled, pausedUntil,
                lastKnownLocationId, lastUpdatedAt);
    }

    // ─── Business methods ───────────────────────────────────────────

    public void pause(Instant pausedUntil) {
        this.isSharingLocation = false;
        this.pausedUntil = pausedUntil;
        this.lastUpdatedAt = Instant.now();
    }

    public void resume() {
        this.isSharingLocation = true;
        this.pausedUntil = null;
        this.lastUpdatedAt = Instant.now();
    }

    public boolean isSharingActive() {
        if (!isSharingLocation) {
            if (pausedUntil != null && pausedUntil.isBefore(Instant.now())) {
                resume();
                return true;
            }
            return false;
        }
        return true;
    }

    public void updateLastKnownLocation(UUID locationId) {
        this.lastKnownLocationId = locationId;
        this.lastUpdatedAt = Instant.now();
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

    public boolean isSharingLocation() {
        return isSharingLocation;
    }

    public boolean isHistoryEnabled() {
        return isHistoryEnabled;
    }

    public Instant getPausedUntil() {
        return pausedUntil;
    }

    public UUID getLastKnownLocationId() {
        return lastKnownLocationId;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }
}
