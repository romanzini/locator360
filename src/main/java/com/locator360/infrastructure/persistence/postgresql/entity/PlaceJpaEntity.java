package com.locator360.infrastructure.persistence.postgresql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "places")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceJpaEntity {

  @Id
  private UUID id;

  @Column(name = "circle_id", nullable = false)
  private UUID circleId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String type;

  @Column(name = "address_text")
  private String addressText;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  @Column(name = "radius_meters", nullable = false)
  private double radiusMeters;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Column(name = "created_by_user_id", nullable = false)
  private UUID createdByUserId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
