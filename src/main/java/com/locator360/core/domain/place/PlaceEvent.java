package com.locator360.core.domain.place;

import java.time.Instant;
import java.util.UUID;

public class PlaceEvent {

    private final UUID id;
    private final UUID placeId;
    private final UUID circleId;
    private final UUID userId;
    private final PlaceEventType eventType;
    private final UUID locationId;
    private final Instant occurredAt;
    private final Instant createdAt;

    private PlaceEvent(UUID id, UUID placeId, UUID circleId, UUID userId,
                       PlaceEventType eventType, UUID locationId,
                       Instant occurredAt, Instant createdAt) {
        this.id = id;
        this.placeId = placeId;
        this.circleId = circleId;
        this.userId = userId;
        this.eventType = eventType;
        this.locationId = locationId;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
    }

    // ─── Factory: criação ───────────────────────────────────────────

    public static PlaceEvent create(UUID placeId, UUID circleId, UUID userId,
                                    PlaceEventType eventType, UUID locationId,
                                    Instant occurredAt) {
        validateCreation(placeId, circleId, userId, eventType, occurredAt);
        return new PlaceEvent(UUID.randomUUID(), placeId, circleId, userId,
                eventType, locationId, occurredAt, Instant.now());
    }

    // ─── Factory: reconstituição ────────────────────────────────────

    public static PlaceEvent restore(UUID id, UUID placeId, UUID circleId, UUID userId,
                                     PlaceEventType eventType, UUID locationId,
                                     Instant occurredAt, Instant createdAt) {
        return new PlaceEvent(id, placeId, circleId, userId,
                eventType, locationId, occurredAt, createdAt);
    }

    // ─── Validations ────────────────────────────────────────────────

    private static void validateCreation(UUID placeId, UUID circleId, UUID userId,
                                         PlaceEventType eventType, Instant occurredAt) {
        if (placeId == null) {
            throw new IllegalArgumentException("Place ID is required");
        }
        if (circleId == null) {
            throw new IllegalArgumentException("Circle ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is required");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred at timestamp is required");
        }
    }

    // ─── Getters ────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public UUID getPlaceId() {
        return placeId;
    }

    public UUID getCircleId() {
        return circleId;
    }

    public UUID getUserId() {
        return userId;
    }

    public PlaceEventType getEventType() {
        return eventType;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
