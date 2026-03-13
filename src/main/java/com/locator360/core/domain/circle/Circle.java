package com.locator360.core.domain.circle;

import java.time.Instant;
import java.util.UUID;

public class Circle {

    private final UUID id;
    private String name;
    private String description;
    private String photoUrl;
    private String colorHex;
    private PrivacyLevel privacyLevel;
    private final UUID createdByUserId;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private Circle(UUID id, String name, String description, String photoUrl,
                   String colorHex, PrivacyLevel privacyLevel, UUID createdByUserId,
                   Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photoUrl = photoUrl;
        this.colorHex = colorHex;
        this.privacyLevel = privacyLevel;
        this.createdByUserId = createdByUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    // ─── Factory: criação ───────────────────────────────────────────

    public static Circle create(String name, String description, String photoUrl,
                                String colorHex, PrivacyLevel privacyLevel, UUID createdByUserId) {
        validateCreation(name, createdByUserId);

        PrivacyLevel level = privacyLevel != null ? privacyLevel : PrivacyLevel.OPEN_WITH_CODE;
        Instant now = Instant.now();

        return new Circle(UUID.randomUUID(), name.trim(), description, photoUrl,
                colorHex, level, createdByUserId, now, now, null);
    }

    // ─── Factory: reconstituição ────────────────────────────────────

    public static Circle restore(UUID id, String name, String description, String photoUrl,
                                 String colorHex, PrivacyLevel privacyLevel, UUID createdByUserId,
                                 Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new Circle(id, name, description, photoUrl, colorHex, privacyLevel,
                createdByUserId, createdAt, updatedAt, deletedAt);
    }

    // ─── Business methods ───────────────────────────────────────────

    public void update(String name, String description, String photoUrl,
                       String colorHex, PrivacyLevel privacyLevel) {
        if (name != null) {
            if (name.isBlank()) {
                throw new IllegalArgumentException("Circle name cannot be blank");
            }
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description;
        }
        if (photoUrl != null) {
            this.photoUrl = photoUrl;
        }
        if (colorHex != null) {
            this.colorHex = colorHex;
        }
        if (privacyLevel != null) {
            this.privacyLevel = privacyLevel;
        }
        this.updatedAt = Instant.now();
    }

    public void delete() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("Circle is already deleted");
        }
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // ─── Validations ────────────────────────────────────────────────

    private static void validateCreation(String name, UUID createdByUserId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Circle name is required");
        }
        if (createdByUserId == null) {
            throw new IllegalArgumentException("Creator user ID is required");
        }
    }

    // ─── Getters ────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getColorHex() {
        return colorHex;
    }

    public PrivacyLevel getPrivacyLevel() {
        return privacyLevel;
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

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
