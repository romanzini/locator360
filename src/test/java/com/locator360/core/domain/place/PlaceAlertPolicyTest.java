package com.locator360.core.domain.place;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlaceAlertPolicyTest {

  private final UUID placeId = UUID.randomUUID();
  private final UUID circleId = UUID.randomUUID();

  @Nested
  @DisplayName("createDefault")
  class CreateDefaultTests {

    @Test
    @DisplayName("should create default policy with enter and exit alerts enabled")
    void shouldCreateDefaultPolicy() {
      PlaceAlertPolicy policy = PlaceAlertPolicy.createDefault(placeId, circleId);

      assertNotNull(policy.getId());
      assertEquals(placeId, policy.getPlaceId());
      assertEquals(circleId, policy.getCircleId());
      assertTrue(policy.isAlertOnEnter());
      assertTrue(policy.isAlertOnExit());
      assertNull(policy.getDaysOfWeek());
      assertNull(policy.getStartTime());
      assertNull(policy.getEndTime());
      assertEquals(TargetType.ALL_MEMBERS, policy.getTargetType());
      assertNotNull(policy.getCreatedAt());
      assertNotNull(policy.getUpdatedAt());
    }

    @Test
    @DisplayName("should throw when placeId is null")
    void shouldThrowWhenPlaceIdNull() {
      assertThrows(IllegalArgumentException.class, () -> PlaceAlertPolicy.createDefault(null, circleId));
    }

    @Test
    @DisplayName("should throw when circleId is null")
    void shouldThrowWhenCircleIdNull() {
      assertThrows(IllegalArgumentException.class, () -> PlaceAlertPolicy.createDefault(placeId, null));
    }
  }

  @Nested
  @DisplayName("restore")
  class RestoreTests {

    @Test
    @DisplayName("should restore policy from persisted data")
    void shouldRestorePolicy() {
      UUID id = UUID.randomUUID();
      PlaceAlertPolicy policy = PlaceAlertPolicy.restore(id, placeId, circleId,
          true, false, "MON,TUE", null, null,
          TargetType.ADMINS_ONLY, java.time.Instant.now(), java.time.Instant.now());

      assertEquals(id, policy.getId());
      assertEquals(placeId, policy.getPlaceId());
      assertEquals(circleId, policy.getCircleId());
      assertTrue(policy.isAlertOnEnter());
      assertFalse(policy.isAlertOnExit());
      assertEquals("MON,TUE", policy.getDaysOfWeek());
      assertEquals(TargetType.ADMINS_ONLY, policy.getTargetType());
    }
  }
}
