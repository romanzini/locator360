package com.locator360.core.domain.place;

import java.util.UUID;

public class PlaceAlertTarget {

  private final UUID id;
  private final UUID policyId;
  private final UUID userId;

  private PlaceAlertTarget(UUID id, UUID policyId, UUID userId) {
    this.id = id;
    this.policyId = policyId;
    this.userId = userId;
  }

  // ─── Factory: criação ───────────────────────────────────────────

  public static PlaceAlertTarget create(UUID policyId, UUID userId) {
    validateCreation(policyId, userId);
    return new PlaceAlertTarget(UUID.randomUUID(), policyId, userId);
  }

  // ─── Factory: reconstituição ────────────────────────────────────

  public static PlaceAlertTarget restore(UUID id, UUID policyId, UUID userId) {
    return new PlaceAlertTarget(id, policyId, userId);
  }

  // ─── Validations ────────────────────────────────────────────────

  private static void validateCreation(UUID policyId, UUID userId) {
    if (policyId == null) {
      throw new IllegalArgumentException("Policy ID is required");
    }
    if (userId == null) {
      throw new IllegalArgumentException("User ID is required");
    }
  }

  // ─── Getters ────────────────────────────────────────────────────

  public UUID getId() {
    return id;
  }

  public UUID getPolicyId() {
    return policyId;
  }

  public UUID getUserId() {
    return userId;
  }
}
