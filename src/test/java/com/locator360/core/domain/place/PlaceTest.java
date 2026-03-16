package com.locator360.core.domain.place;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlaceTest {

  private final UUID circleId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();

  // ─── create ─────────────────────────────────────────────────────

  @Nested
  @DisplayName("create")
  class CreateTests {

    @Test
    @DisplayName("should create place with valid data")
    void shouldCreatePlaceWithValidData() {
      Place place = Place.create(circleId, "Casa", PlaceType.HOME,
          "Rua A, 123", -23.5, -46.6, 100.0, userId);

      assertNotNull(place.getId());
      assertEquals(circleId, place.getCircleId());
      assertEquals("Casa", place.getName());
      assertEquals(PlaceType.HOME, place.getType());
      assertEquals("Rua A, 123", place.getAddressText());
      assertEquals(-23.5, place.getLatitude());
      assertEquals(-46.6, place.getLongitude());
      assertEquals(100.0, place.getRadiusMeters());
      assertTrue(place.isActive());
      assertEquals(userId, place.getCreatedByUserId());
      assertNotNull(place.getCreatedAt());
      assertNotNull(place.getUpdatedAt());
    }

    @Test
    @DisplayName("should trim place name")
    void shouldTrimPlaceName() {
      Place place = Place.create(circleId, "  Casa  ", PlaceType.HOME,
          null, -23.5, -46.6, 100.0, userId);

      assertEquals("Casa", place.getName());
    }

    @Test
    @DisplayName("should default to OTHER type when null")
    void shouldDefaultToOtherType() {
      Place place = Place.create(circleId, "Local", null,
          null, -23.5, -46.6, 100.0, userId);

      assertEquals(PlaceType.OTHER, place.getType());
    }

    @Test
    @DisplayName("should throw when circleId is null")
    void shouldThrowWhenCircleIdNull() {
      assertThrows(IllegalArgumentException.class, () -> Place.create(null, "Casa", PlaceType.HOME,
          null, -23.5, -46.6, 100.0, userId));
    }

    @Test
    @DisplayName("should throw when name is null")
    void shouldThrowWhenNameNull() {
      assertThrows(IllegalArgumentException.class, () -> Place.create(circleId, null, PlaceType.HOME,
          null, -23.5, -46.6, 100.0, userId));
    }

    @Test
    @DisplayName("should throw when name is blank")
    void shouldThrowWhenNameBlank() {
      assertThrows(IllegalArgumentException.class, () -> Place.create(circleId, "   ", PlaceType.HOME,
          null, -23.5, -46.6, 100.0, userId));
    }

    @Test
    @DisplayName("should throw when createdByUserId is null")
    void shouldThrowWhenCreatedByUserIdNull() {
      assertThrows(IllegalArgumentException.class, () -> Place.create(circleId, "Casa", PlaceType.HOME,
          null, -23.5, -46.6, 100.0, null));
    }

    @Test
    @DisplayName("should throw when latitude out of range")
    void shouldThrowWhenLatitudeOutOfRange() {
      assertThrows(IllegalArgumentException.class, () -> Place.create(circleId, "Casa", PlaceType.HOME,
          null, -91.0, -46.6, 100.0, userId));
      assertThrows(IllegalArgumentException.class, () -> Place.create(circleId, "Casa", PlaceType.HOME,
          null, 91.0, -46.6, 100.0, userId));
    }

    @Test
    @DisplayName("should throw when longitude out of range")
    void shouldThrowWhenLongitudeOutOfRange() {
      assertThrows(IllegalArgumentException.class, () -> Place.create(circleId, "Casa", PlaceType.HOME,
          null, -23.5, -181.0, 100.0, userId));
      assertThrows(IllegalArgumentException.class, () -> Place.create(circleId, "Casa", PlaceType.HOME,
          null, -23.5, 181.0, 100.0, userId));
    }

    @Test
    @DisplayName("should throw when radius is zero or negative")
    void shouldThrowWhenRadiusInvalid() {
      assertThrows(IllegalArgumentException.class, () -> Place.create(circleId, "Casa", PlaceType.HOME,
          null, -23.5, -46.6, 0.0, userId));
      assertThrows(IllegalArgumentException.class, () -> Place.create(circleId, "Casa", PlaceType.HOME,
          null, -23.5, -46.6, -10.0, userId));
    }
  }

  // ─── update ─────────────────────────────────────────────────────

  @Nested
  @DisplayName("update")
  class UpdateTests {

    @Test
    @DisplayName("should update name")
    void shouldUpdateName() {
      Place place = Place.create(circleId, "Casa", PlaceType.HOME,
          null, -23.5, -46.6, 100.0, userId);

      place.update("Escola", null, null, null, null, null);

      assertEquals("Escola", place.getName());
    }

    @Test
    @DisplayName("should throw when updating with blank name")
    void shouldThrowWhenUpdatingWithBlankName() {
      Place place = Place.create(circleId, "Casa", PlaceType.HOME,
          null, -23.5, -46.6, 100.0, userId);

      assertThrows(IllegalArgumentException.class, () -> place.update("   ", null, null, null, null, null));
    }

    @Test
    @DisplayName("should update coordinates")
    void shouldUpdateCoordinates() {
      Place place = Place.create(circleId, "Casa", PlaceType.HOME,
          null, -23.5, -46.6, 100.0, userId);

      place.update(null, null, null, -22.0, -45.0, null);

      assertEquals(-22.0, place.getLatitude());
      assertEquals(-45.0, place.getLongitude());
    }

    @Test
    @DisplayName("should throw when update latitude out of range")
    void shouldThrowWhenUpdateLatitudeOutOfRange() {
      Place place = Place.create(circleId, "Casa", PlaceType.HOME,
          null, -23.5, -46.6, 100.0, userId);

      assertThrows(IllegalArgumentException.class, () -> place.update(null, null, null, -91.0, null, null));
    }

    @Test
    @DisplayName("should throw when update radius invalid")
    void shouldThrowWhenUpdateRadiusInvalid() {
      Place place = Place.create(circleId, "Casa", PlaceType.HOME,
          null, -23.5, -46.6, 100.0, userId);

      assertThrows(IllegalArgumentException.class, () -> place.update(null, null, null, null, null, 0.0));
    }

    @Test
    @DisplayName("should not change fields when null is passed")
    void shouldNotChangeFieldsWhenNull() {
      Place place = Place.create(circleId, "Casa", PlaceType.HOME,
          "Rua A", -23.5, -46.6, 100.0, userId);

      place.update(null, null, null, null, null, null);

      assertEquals("Casa", place.getName());
      assertEquals(PlaceType.HOME, place.getType());
      assertEquals("Rua A", place.getAddressText());
      assertEquals(-23.5, place.getLatitude());
      assertEquals(-46.6, place.getLongitude());
      assertEquals(100.0, place.getRadiusMeters());
    }
  }

  // ─── deactivate ─────────────────────────────────────────────────

  @Nested
  @DisplayName("deactivate")
  class DeactivateTests {

    @Test
    @DisplayName("should deactivate active place")
    void shouldDeactivateActivePlace() {
      Place place = Place.create(circleId, "Casa", PlaceType.HOME,
          null, -23.5, -46.6, 100.0, userId);

      place.deactivate();

      assertFalse(place.isActive());
    }

    @Test
    @DisplayName("should throw when deactivating inactive place")
    void shouldThrowWhenDeactivatingInactivePlace() {
      Place place = Place.restore(UUID.randomUUID(), circleId, "Casa",
          PlaceType.HOME, null, -23.5, -46.6, 100.0, false,
          userId, Instant.now(), Instant.now());

      assertThrows(IllegalStateException.class, place::deactivate);
    }
  }

  // ─── restore ────────────────────────────────────────────────────

  @Nested
  @DisplayName("restore")
  class RestoreTests {

    @Test
    @DisplayName("should restore place from persisted data")
    void shouldRestorePlace() {
      UUID id = UUID.randomUUID();
      Instant now = Instant.now();

      Place place = Place.restore(id, circleId, "Casa", PlaceType.HOME,
          "Rua A", -23.5, -46.6, 100.0, true, userId, now, now);

      assertEquals(id, place.getId());
      assertEquals(circleId, place.getCircleId());
      assertEquals("Casa", place.getName());
      assertEquals(PlaceType.HOME, place.getType());
      assertEquals("Rua A", place.getAddressText());
      assertTrue(place.isActive());
      assertEquals(userId, place.getCreatedByUserId());
      assertEquals(now, place.getCreatedAt());
    }
  }
}
