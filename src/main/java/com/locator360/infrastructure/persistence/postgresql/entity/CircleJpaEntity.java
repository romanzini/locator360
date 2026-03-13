package com.locator360.infrastructure.persistence.postgresql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "circles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CircleJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "color_hex")
    private String colorHex;

    @Column(name = "privacy_level", nullable = false)
    private String privacyLevel;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
