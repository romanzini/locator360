package com.locator360.infrastructure.persistence.postgresql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "place_alert_targets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceAlertTargetJpaEntity {

  @Id
  private UUID id;

  @Column(name = "policy_id", nullable = false)
  private UUID policyId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;
}
