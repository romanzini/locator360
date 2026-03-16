package com.locator360.core.domain.location;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    // ─── Factory create() ───────────────────────────────────────────

    @Nested
    @DisplayName("Location.create()")
    class CreateTests {

        private final UUID userId = UUID.randomUUID();
        private final UUID circleId = UUID.randomUUID();
        private final Instant recordedAt = Instant.now();

        @Test
        @DisplayName("should create location with all fields")
        void shouldCreateLocationWithAllFields() {
            Location location = Location.create(userId, circleId, -23.561414, -46.655881,
                    10.5, 5.0, 180.0, 760.0, LocationSource.GPS, recordedAt, true, 72);

            assertNotNull(location.getId());
            assertEquals(userId, location.getUserId());
            assertEquals(circleId, location.getCircleId());
            assertEquals(-23.561414, location.getLatitude());
            assertEquals(-46.655881, location.getLongitude());
            assertEquals(10.5, location.getAccuracyMeters());
            assertEquals(5.0, location.getSpeedMps());
            assertEquals(180.0, location.getHeadingDegrees());
            assertEquals(760.0, location.getAltitudeMeters());
            assertEquals(LocationSource.GPS, location.getSource());
            assertEquals(recordedAt, location.getRecordedAt());
            assertNotNull(location.getReceivedAt());
            assertTrue(location.isMoving());
            assertEquals(72, location.getBatteryLevel());
            assertNotNull(location.getCreatedAt());
        }

        @Test
        @DisplayName("should create location with only required fields")
        void shouldCreateLocationWithOnlyRequiredFields() {
            Location location = Location.create(userId, null, 0.0, 0.0,
                    null, null, null, null, LocationSource.NETWORK, recordedAt, false, null);

            assertNotNull(location.getId());
            assertEquals(userId, location.getUserId());
            assertNull(location.getCircleId());
            assertEquals(0.0, location.getLatitude());
            assertEquals(0.0, location.getLongitude());
            assertNull(location.getAccuracyMeters());
            assertNull(location.getSpeedMps());
            assertNull(location.getHeadingDegrees());
            assertNull(location.getAltitudeMeters());
            assertEquals(LocationSource.NETWORK, location.getSource());
            assertFalse(location.isMoving());
            assertNull(location.getBatteryLevel());
        }

        @Test
        @DisplayName("should create location with FUSED source")
        void shouldCreateLocationWithFusedSource() {
            Location location = Location.create(userId, null, 10.0, 20.0,
                    null, null, null, null, LocationSource.FUSED, recordedAt, false, null);

            assertEquals(LocationSource.FUSED, location.getSource());
        }

        @Test
        @DisplayName("should throw when userId is null")
        void shouldThrowWhenUserIdIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> Location.create(null, circleId, -23.0, -46.0,
                            null, null, null, null, LocationSource.GPS, recordedAt, false, null));

            assertTrue(ex.getMessage().toLowerCase().contains("user"));
        }

        @Test
        @DisplayName("should throw when latitude is below -90")
        void shouldThrowWhenLatitudeBelowRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> Location.create(userId, null, -91.0, -46.0,
                            null, null, null, null, LocationSource.GPS, recordedAt, false, null));
        }

        @Test
        @DisplayName("should throw when latitude is above 90")
        void shouldThrowWhenLatitudeAboveRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> Location.create(userId, null, 91.0, -46.0,
                            null, null, null, null, LocationSource.GPS, recordedAt, false, null));
        }

        @Test
        @DisplayName("should throw when longitude is below -180")
        void shouldThrowWhenLongitudeBelowRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> Location.create(userId, null, -23.0, -181.0,
                            null, null, null, null, LocationSource.GPS, recordedAt, false, null));
        }

        @Test
        @DisplayName("should throw when longitude is above 180")
        void shouldThrowWhenLongitudeAboveRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> Location.create(userId, null, -23.0, 181.0,
                            null, null, null, null, LocationSource.GPS, recordedAt, false, null));
        }

        @Test
        @DisplayName("should accept boundary latitude values")
        void shouldAcceptBoundaryLatitudeValues() {
            assertDoesNotThrow(() -> Location.create(userId, null, -90.0, 0.0,
                    null, null, null, null, LocationSource.GPS, recordedAt, false, null));
            assertDoesNotThrow(() -> Location.create(userId, null, 90.0, 0.0,
                    null, null, null, null, LocationSource.GPS, recordedAt, false, null));
        }

        @Test
        @DisplayName("should accept boundary longitude values")
        void shouldAcceptBoundaryLongitudeValues() {
            assertDoesNotThrow(() -> Location.create(userId, null, 0.0, -180.0,
                    null, null, null, null, LocationSource.GPS, recordedAt, false, null));
            assertDoesNotThrow(() -> Location.create(userId, null, 0.0, 180.0,
                    null, null, null, null, LocationSource.GPS, recordedAt, false, null));
        }

        @Test
        @DisplayName("should throw when source is null")
        void shouldThrowWhenSourceIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> Location.create(userId, null, -23.0, -46.0,
                            null, null, null, null, null, recordedAt, false, null));
        }

        @Test
        @DisplayName("should throw when recordedAt is null")
        void shouldThrowWhenRecordedAtIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> Location.create(userId, null, -23.0, -46.0,
                            null, null, null, null, LocationSource.GPS, null, false, null));
        }

        @Test
        @DisplayName("should throw when battery level is below 0")
        void shouldThrowWhenBatteryLevelBelowRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> Location.create(userId, null, -23.0, -46.0,
                            null, null, null, null, LocationSource.GPS, recordedAt, false, -1));
        }

        @Test
        @DisplayName("should throw when battery level is above 100")
        void shouldThrowWhenBatteryLevelAboveRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> Location.create(userId, null, -23.0, -46.0,
                            null, null, null, null, LocationSource.GPS, recordedAt, false, 101));
        }

        @Test
        @DisplayName("should accept boundary battery level values")
        void shouldAcceptBoundaryBatteryLevelValues() {
            assertDoesNotThrow(() -> Location.create(userId, null, -23.0, -46.0,
                    null, null, null, null, LocationSource.GPS, recordedAt, false, 0));
            assertDoesNotThrow(() -> Location.create(userId, null, -23.0, -46.0,
                    null, null, null, null, LocationSource.GPS, recordedAt, false, 100));
        }
    }

    // ─── Factory restore() ──────────────────────────────────────────

    @Nested
    @DisplayName("Location.restore()")
    class RestoreTests {

        @Test
        @DisplayName("should restore location with all fields")
        void shouldRestoreLocationWithAllFields() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID circleId = UUID.randomUUID();
            Instant recordedAt = Instant.now();
            Instant receivedAt = Instant.now();
            Instant createdAt = Instant.now();

            Location location = Location.restore(id, userId, circleId, -23.561, -46.655,
                    10.5, 5.0, 180.0, 760.0, LocationSource.GPS, recordedAt, receivedAt,
                    true, 72, createdAt);

            assertEquals(id, location.getId());
            assertEquals(userId, location.getUserId());
            assertEquals(circleId, location.getCircleId());
            assertEquals(-23.561, location.getLatitude());
            assertEquals(-46.655, location.getLongitude());
            assertEquals(10.5, location.getAccuracyMeters());
            assertEquals(5.0, location.getSpeedMps());
            assertEquals(180.0, location.getHeadingDegrees());
            assertEquals(760.0, location.getAltitudeMeters());
            assertEquals(LocationSource.GPS, location.getSource());
            assertEquals(recordedAt, location.getRecordedAt());
            assertEquals(receivedAt, location.getReceivedAt());
            assertTrue(location.isMoving());
            assertEquals(72, location.getBatteryLevel());
            assertEquals(createdAt, location.getCreatedAt());
        }

        @Test
        @DisplayName("should restore location with nullable fields as null")
        void shouldRestoreLocationWithNullableFieldsAsNull() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Instant recordedAt = Instant.now();
            Instant receivedAt = Instant.now();
            Instant createdAt = Instant.now();

            Location location = Location.restore(id, userId, null, 0.0, 0.0,
                    null, null, null, null, LocationSource.NETWORK, recordedAt, receivedAt,
                    false, null, createdAt);

            assertEquals(id, location.getId());
            assertNull(location.getCircleId());
            assertNull(location.getAccuracyMeters());
            assertNull(location.getSpeedMps());
            assertNull(location.getHeadingDegrees());
            assertNull(location.getAltitudeMeters());
            assertNull(location.getBatteryLevel());
        }
    }
}
