package com.locator360.core.domain.circle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CircleSettingsTest {

    // ─── Factory createDefault() ────────────────────────────────────

    @Nested
    @DisplayName("CircleSettings.createDefault()")
    class CreateDefaultTests {

        @Test
        @DisplayName("should create settings with default values")
        void shouldCreateSettingsWithDefaults() {
            UUID circleId = UUID.randomUUID();

            CircleSettings settings = CircleSettings.createDefault(circleId);

            assertNotNull(settings.getId());
            assertEquals(circleId, settings.getCircleId());
            assertEquals(DrivingAlertLevel.MEDIUM, settings.getDrivingAlertLevel());
            assertTrue(settings.isAllowMemberChat());
            assertTrue(settings.isAllowMemberSos());
            assertNotNull(settings.getCreatedAt());
            assertNotNull(settings.getUpdatedAt());
        }

        @Test
        @DisplayName("should throw when circle ID is null")
        void shouldThrowWhenCircleIdIsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> CircleSettings.createDefault(null));

            assertTrue(ex.getMessage().toLowerCase().contains("circle id"));
        }
    }

    // ─── Factory restore() ──────────────────────────────────────────

    @Nested
    @DisplayName("CircleSettings.restore()")
    class RestoreTests {

        @Test
        @DisplayName("should restore settings with all fields")
        void shouldRestoreSettingsWithAllFields() {
            UUID id = UUID.randomUUID();
            UUID circleId = UUID.randomUUID();
            Instant now = Instant.now();

            CircleSettings settings = CircleSettings.restore(id, circleId,
                    DrivingAlertLevel.HIGH, false, false, now, now);

            assertEquals(id, settings.getId());
            assertEquals(circleId, settings.getCircleId());
            assertEquals(DrivingAlertLevel.HIGH, settings.getDrivingAlertLevel());
            assertFalse(settings.isAllowMemberChat());
            assertFalse(settings.isAllowMemberSos());
            assertEquals(now, settings.getCreatedAt());
            assertEquals(now, settings.getUpdatedAt());
        }
    }

    // ─── Business methods ───────────────────────────────────────────

    @Nested
    @DisplayName("CircleSettings.update()")
    class UpdateTests {

        @Test
        @DisplayName("should update all fields")
        void shouldUpdateAllFields() {
            CircleSettings settings = CircleSettings.createDefault(UUID.randomUUID());

            settings.update(DrivingAlertLevel.LOW, false, false);

            assertEquals(DrivingAlertLevel.LOW, settings.getDrivingAlertLevel());
            assertFalse(settings.isAllowMemberChat());
            assertFalse(settings.isAllowMemberSos());
        }

        @Test
        @DisplayName("should update only non-null fields")
        void shouldUpdateOnlyNonNullFields() {
            CircleSettings settings = CircleSettings.createDefault(UUID.randomUUID());

            settings.update(DrivingAlertLevel.HIGH, null, null);

            assertEquals(DrivingAlertLevel.HIGH, settings.getDrivingAlertLevel());
            assertTrue(settings.isAllowMemberChat());
            assertTrue(settings.isAllowMemberSos());
        }

        @Test
        @DisplayName("should update updatedAt timestamp")
        void shouldUpdateUpdatedAtTimestamp() {
            CircleSettings settings = CircleSettings.createDefault(UUID.randomUUID());
            Instant before = settings.getUpdatedAt();

            settings.update(DrivingAlertLevel.LOW, null, null);

            assertTrue(settings.getUpdatedAt().compareTo(before) >= 0);
        }
    }
}
