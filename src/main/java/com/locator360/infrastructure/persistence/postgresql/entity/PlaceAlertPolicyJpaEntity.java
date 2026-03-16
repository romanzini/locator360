package com.locator360.infrastructure.persistence.postgresql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "place_alert_policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceAlertPolicyJpaEntity {

  @Id
  private UUID id;

  @Column(name = "place_id", nullable = false)
  private UUID placeId;

  @Column(name = "circle_id", nullable = false)
  private UUID circleId;

  @Column(name = "alert_on_enter", nullable = false)
  private boolean alertOnEnter;

  @Column(name = "alert_on_exit", nullable = false)
  private boolean alertOnExit;

  @Column(name = "days_of_week")
  private String daysOfWeek;

  @Column(name = "start_time")
  private LocalTime startTime;

  @Column(name = "end_time")
  private LocalTime endTime;

  @Column(name = "target_type", nullable = false)
  private String targetType;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
