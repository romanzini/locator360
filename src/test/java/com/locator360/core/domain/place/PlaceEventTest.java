package com.locator360.core.domain.place;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlaceEventTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create place event with valid data")
        void shouldCreateWithValidData() {
            UUID placeId = UUID.randomUUID();
            UUID circleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID locationId = UUID.randomUUID();
            Instant occurredAt = Instant.now();

            PlaceEvent event = PlaceEvent.create(placeId, circleId, userId,
                    PlaceEventType.ENTER, locationId, occurredAt);

            assertNotNull(event.getId());
            assertEquals(placeId, event.getPlaceId());
            assertEquals(circleId, event.getCircleId());
            assertEquals(userId, event.getUserId());
            assertEquals(PlaceEventType.ENTER, event.getEventType());
            assertEquals(locationId, event.getLocationId());
            assertEquals(occurredAt, event.getOccurredAt());
            assertNotNull(event.getCreatedAt());
        }

        @Test
        @DisplayName("should create place event with null locationId")
        void shouldCreateWithNullLocationId() {
            PlaceEvent event = PlaceEvent.create(UUID.randomUUID(), UUID.randomUUID(),
                    UUID.randomUUID(), PlaceEventType.EXIT, null, Instant.now());

            assertNull(event.getLocationId());
            assertEquals(PlaceEventType.EXIT, event.getEventType());
        }

        @Test
        @DisplayName("should throw when placeId is null")
        void shouldThrowWhenPlaceIdNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    PlaceEvent.create(null, UUID.randomUUID(), UUID.randomUUID(),
                            PlaceEventType.ENTER, null, Instant.now()));
        }

        @Test
        @DisplayName("should throw when circleId is null")
        void shouldThrowWhenCircleIdNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    PlaceEvent.create(UUID.randomUUID(), null, UUID.randomUUID(),
                            PlaceEventType.ENTER, null, Instant.now()));
        }

        @Test
        @DisplayName("should throw when userId is null")
        void shouldThrowWhenUserIdNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    PlaceEvent.create(UUID.randomUUID(), UUID.randomUUID(), null,
                            PlaceEventType.ENTER, null, Instant.now()));
        }

        @Test
        @DisplayName("should throw when eventType is null")
        void shouldThrowWhenEventTypeNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    PlaceEvent.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                            null, null, Instant.now()));
        }

        @Test
        @DisplayName("should throw when occurredAt is null")
        void shouldThrowWhenOccurredAtNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    PlaceEvent.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                            PlaceEventType.ENTER, null, null));
        }
    }

    @Nested
    @DisplayName("restore")
    class RestoreTests {

        @Test
        @DisplayName("should restore place event from persisted data")
        void shouldRestoreFromPersistedData() {
            UUID id = UUID.randomUUID();
            UUID placeId = UUID.randomUUID();
            UUID circleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID locationId = UUID.randomUUID();
            Instant occurredAt = Instant.now().minusSeconds(60);
            Instant createdAt = Instant.now();

            PlaceEvent event = PlaceEvent.restore(id, placeId, circleId, userId,
                    PlaceEventType.EXIT, locationId, occurredAt, createdAt);

            assertEquals(id, event.getId());
            assertEquals(placeId, event.getPlaceId());
            assertEquals(circleId, event.getCircleId());
            assertEquals(userId, event.getUserId());
            assertEquals(PlaceEventType.EXIT, event.getEventType());
            assertEquals(locationId, event.getLocationId());
            assertEquals(occurredAt, event.getOccurredAt());
            assertEquals(createdAt, event.getCreatedAt());
        }
    }
}
