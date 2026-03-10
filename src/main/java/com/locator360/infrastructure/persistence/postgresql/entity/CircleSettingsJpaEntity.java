package com.locator360.infrastructure.persistence.postgresql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "circle_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CircleSettingsJpaEntity {

    @Id
    private UUID id;

    @Column(name = "circle_id", nullable = false, unique = true)
    private UUID circleId;

    @Column(name = "driving_alert_level", nullable = false)
    private String drivingAlertLevel;

    @Column(name = "allow_member_chat", nullable = false)
    private boolean allowMemberChat;

    @Column(name = "allow_member_sos", nullable = false)
    private boolean allowMemberSos;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
