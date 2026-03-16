package com.locator360.infrastructure.persistence.postgresql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "location_sharing_states")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationSharingStateJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "circle_id", nullable = false)
    private UUID circleId;

    @Column(name = "is_sharing_location", nullable = false)
    private boolean isSharingLocation;

    @Column(name = "is_history_enabled", nullable = false)
    private boolean isHistoryEnabled;

    @Column(name = "paused_until")
    private Instant pausedUntil;

    @Column(name = "last_known_location_id")
    private UUID lastKnownLocationId;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;
}
