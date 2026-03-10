package com.locator360.core.domain.circle;

import java.time.Instant;
import java.util.UUID;

public class CircleSettings {

    private final UUID id;
    private final UUID circleId;
    private DrivingAlertLevel drivingAlertLevel;
    private boolean allowMemberChat;
    private boolean allowMemberSos;
    private final Instant createdAt;
    private Instant updatedAt;

    private CircleSettings(UUID id, UUID circleId, DrivingAlertLevel drivingAlertLevel,
                           boolean allowMemberChat, boolean allowMemberSos,
                           Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.circleId = circleId;
        this.drivingAlertLevel = drivingAlertLevel;
        this.allowMemberChat = allowMemberChat;
        this.allowMemberSos = allowMemberSos;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─── Factory: criação com defaults ──────────────────────────────

    public static CircleSettings createDefault(UUID circleId) {
        if (circleId == null) {
            throw new IllegalArgumentException("Circle ID is required");
        }
        Instant now = Instant.now();
        return new CircleSettings(UUID.randomUUID(), circleId,
                DrivingAlertLevel.MEDIUM, true, true, now, now);
    }

    // ─── Factory: reconstituição ────────────────────────────────────

    public static CircleSettings restore(UUID id, UUID circleId, DrivingAlertLevel drivingAlertLevel,
                                         boolean allowMemberChat, boolean allowMemberSos,
                                         Instant createdAt, Instant updatedAt) {
        return new CircleSettings(id, circleId, drivingAlertLevel,
                allowMemberChat, allowMemberSos, createdAt, updatedAt);
    }

    // ─── Business methods ───────────────────────────────────────────

    public void update(DrivingAlertLevel drivingAlertLevel, Boolean allowMemberChat,
                       Boolean allowMemberSos) {
        if (drivingAlertLevel != null) {
            this.drivingAlertLevel = drivingAlertLevel;
        }
        if (allowMemberChat != null) {
            this.allowMemberChat = allowMemberChat;
        }
        if (allowMemberSos != null) {
            this.allowMemberSos = allowMemberSos;
        }
        this.updatedAt = Instant.now();
    }

    // ─── Getters ────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public UUID getCircleId() {
        return circleId;
    }

    public DrivingAlertLevel getDrivingAlertLevel() {
        return drivingAlertLevel;
    }

    public boolean isAllowMemberChat() {
        return allowMemberChat;
    }

    public boolean isAllowMemberSos() {
        return allowMemberSos;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
