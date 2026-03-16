package com.locator360.infrastructure.persistence.postgresql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "circle_id")
    private UUID circleId;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "accuracy_meters")
    private Double accuracyMeters;

    @Column(name = "speed_mps")
    private Double speedMps;

    @Column(name = "heading_degrees")
    private Double headingDegrees;

    @Column(name = "altitude_meters")
    private Double altitudeMeters;

    @Column(nullable = false)
    private String source;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "is_moving", nullable = false)
    private boolean isMoving;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
