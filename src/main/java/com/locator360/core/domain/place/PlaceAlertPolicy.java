package com.locator360.core.domain.place;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public class PlaceAlertPolicy {

  private final UUID id;
  private final UUID placeId;
  private final UUID circleId;
  private boolean alertOnEnter;
  private boolean alertOnExit;
  private String daysOfWeek;
  private LocalTime startTime;
  private LocalTime endTime;
  private TargetType targetType;
  private final Instant createdAt;
  private Instant updatedAt;

  private PlaceAlertPolicy(UUID id, UUID placeId, UUID circleId, boolean alertOnEnter,
      boolean alertOnExit, String daysOfWeek, LocalTime startTime,
      LocalTime endTime, TargetType targetType,
      Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.placeId = placeId;
    this.circleId = circleId;
    this.alertOnEnter = alertOnEnter;
    this.alertOnExit = alertOnExit;
    this.daysOfWeek = daysOfWeek;
    this.startTime = startTime;
    this.endTime = endTime;
    this.targetType = targetType;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // ─── Factory: criação com defaults ──────────────────────────────

  public static PlaceAlertPolicy createDefault(UUID placeId, UUID circleId) {
    validateCreation(placeId, circleId);
    Instant now = Instant.now();
    return new PlaceAlertPolicy(UUID.randomUUID(), placeId, circleId,
        true, true, null, null, null,
        TargetType.ALL_MEMBERS, now, now);
  }

  // ─── Factory: reconstituição ────────────────────────────────────

  public static PlaceAlertPolicy restore(UUID id, UUID placeId, UUID circleId,
      boolean alertOnEnter, boolean alertOnExit,
      String daysOfWeek, LocalTime startTime,
      LocalTime endTime, TargetType targetType,
      Instant createdAt, Instant updatedAt) {
    return new PlaceAlertPolicy(id, placeId, circleId, alertOnEnter, alertOnExit,
        daysOfWeek, startTime, endTime, targetType, createdAt, updatedAt);
  }

  // ─── Validations ────────────────────────────────────────────────

  private static void validateCreation(UUID placeId, UUID circleId) {
    if (placeId == null) {
      throw new IllegalArgumentException("Place ID is required");
    }
    if (circleId == null) {
      throw new IllegalArgumentException("Circle ID is required");
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

  public boolean isAlertOnEnter() {
    return alertOnEnter;
  }

  public boolean isAlertOnExit() {
    return alertOnExit;
  }

  public String getDaysOfWeek() {
    return daysOfWeek;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public TargetType getTargetType() {
    return targetType;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
