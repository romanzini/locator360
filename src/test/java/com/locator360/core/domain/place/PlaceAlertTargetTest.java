package com.locator360.core.domain.place;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlaceAlertTargetTest {

  @Nested
  @DisplayName("create")
  class CreateTests {

    @Test
    @DisplayName("should create target with valid data")
    void shouldCreateTarget() {
      UUID policyId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      PlaceAlertTarget target = PlaceAlertTarget.create(policyId, userId);

      assertNotNull(target.getId());
      assertEquals(policyId, target.getPolicyId());
      assertEquals(userId, target.getUserId());
    }

    @Test
    @DisplayName("should throw when policyId is null")
    void shouldThrowWhenPolicyIdNull() {
      assertThrows(IllegalArgumentException.class, () -> PlaceAlertTarget.create(null, UUID.randomUUID()));
    }

    @Test
    @DisplayName("should throw when userId is null")
    void shouldThrowWhenUserIdNull() {
      assertThrows(IllegalArgumentException.class, () -> PlaceAlertTarget.create(UUID.randomUUID(), null));
    }
  }

  @Nested
  @DisplayName("restore")
  class RestoreTests {

    @Test
    @DisplayName("should restore target from persisted data")
    void shouldRestoreTarget() {
      UUID id = UUID.randomUUID();
      UUID policyId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      PlaceAlertTarget target = PlaceAlertTarget.restore(id, policyId, userId);

      assertEquals(id, target.getId());
      assertEquals(policyId, target.getPolicyId());
      assertEquals(userId, target.getUserId());
    }
  }
}
