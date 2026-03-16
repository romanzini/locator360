package com.locator360.core.domain.location;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocationSharingStateTest {

    // ─── Factory create() ───────────────────────────────────────────

    @Nested
    @DisplayName("LocationSharingState.create()")
    class CreateTests {

        @Test
        @DisplayName("should create sharing state with defaults")
        void shouldCreateSharingStateWithDefaults() {
            UUID userId = UUID.randomUUID();
            UUID circleId = UUID.randomUUID();

            LocationSharingState state = LocationSharingState.create(userId, circleId);

            assertNotNull(state.getId());
            assertEquals(userId, state.getUserId());
            assertEquals(circleId, state.getCircleId());
            assertTrue(state.isSharingLocation());
            assertTrue(state.isHistoryEnabled());
            assertNull(state.getPausedUntil());
            assertNull(state.getLastKnownLocationId());
            assertNotNull(state.getLastUpdatedAt());
        }

        @Test
        @DisplayName("should throw when userId is null")
        void shouldThrowWhenUserIdIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> LocationSharingState.create(null, UUID.randomUUID()));
        }

        @Test
        @DisplayName("should throw when circleId is null")
        void shouldThrowWhenCircleIdIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> LocationSharingState.create(UUID.randomUUID(), null));
        }
    }

    // ─── Factory restore() ──────────────────────────────────────────

    @Nested
    @DisplayName("LocationSharingState.restore()")
    class RestoreTests {

        @Test
        @DisplayName("should restore sharing state with all fields")
        void shouldRestoreSharingStateWithAllFields() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID circleId = UUID.randomUUID();
            UUID locationId = UUID.randomUUID();
            Instant pausedUntil = Instant.now().plus(1, ChronoUnit.HOURS);
            Instant lastUpdatedAt = Instant.now();

            LocationSharingState state = LocationSharingState.restore(
                    id, userId, circleId, false, true, pausedUntil, locationId, lastUpdatedAt);

            assertEquals(id, state.getId());
            assertEquals(userId, state.getUserId());
            assertEquals(circleId, state.getCircleId());
            assertFalse(state.isSharingLocation());
            assertTrue(state.isHistoryEnabled());
            assertEquals(pausedUntil, state.getPausedUntil());
            assertEquals(locationId, state.getLastKnownLocationId());
            assertEquals(lastUpdatedAt, state.getLastUpdatedAt());
        }
    }

    // ─── Business methods ───────────────────────────────────────────

    @Nested
    @DisplayName("pause()")
    class PauseTests {

        @Test
        @DisplayName("should pause sharing with time limit")
        void shouldPauseSharingWithTimeLimit() {
            LocationSharingState state = LocationSharingState.create(UUID.randomUUID(), UUID.randomUUID());
            Instant pausedUntil = Instant.now().plus(1, ChronoUnit.HOURS);

            state.pause(pausedUntil);

            assertFalse(state.isSharingLocation());
            assertEquals(pausedUntil, state.getPausedUntil());
        }

        @Test
        @DisplayName("should pause sharing indefinitely when pausedUntil is null")
        void shouldPauseSharingIndefinitely() {
            LocationSharingState state = LocationSharingState.create(UUID.randomUUID(), UUID.randomUUID());

            state.pause(null);

            assertFalse(state.isSharingLocation());
            assertNull(state.getPausedUntil());
        }
    }

    @Nested
    @DisplayName("resume()")
    class ResumeTests {

        @Test
        @DisplayName("should resume sharing and clear pausedUntil")
        void shouldResumeSharingAndClearPausedUntil() {
            LocationSharingState state = LocationSharingState.create(UUID.randomUUID(), UUID.randomUUID());
            state.pause(Instant.now().plus(1, ChronoUnit.HOURS));

            state.resume();

            assertTrue(state.isSharingLocation());
            assertNull(state.getPausedUntil());
        }
    }

    @Nested
    @DisplayName("isSharingActive()")
    class IsSharingActiveTests {

        @Test
        @DisplayName("should return true when sharing is enabled")
        void shouldReturnTrueWhenSharingEnabled() {
            LocationSharingState state = LocationSharingState.create(UUID.randomUUID(), UUID.randomUUID());

            assertTrue(state.isSharingActive());
        }

        @Test
        @DisplayName("should return false when paused indefinitely")
        void shouldReturnFalseWhenPausedIndefinitely() {
            LocationSharingState state = LocationSharingState.create(UUID.randomUUID(), UUID.randomUUID());
            state.pause(null);

            assertFalse(state.isSharingActive());
        }

        @Test
        @DisplayName("should return false when paused until future time")
        void shouldReturnFalseWhenPausedUntilFuture() {
            LocationSharingState state = LocationSharingState.create(UUID.randomUUID(), UUID.randomUUID());
            state.pause(Instant.now().plus(1, ChronoUnit.HOURS));

            assertFalse(state.isSharingActive());
        }

        @Test
        @DisplayName("should auto-resume when pausedUntil is in the past")
        void shouldAutoResumeWhenPausedUntilInPast() {
            LocationSharingState state = LocationSharingState.create(UUID.randomUUID(), UUID.randomUUID());
            state.pause(Instant.now().minus(1, ChronoUnit.HOURS));

            assertTrue(state.isSharingActive());
            assertTrue(state.isSharingLocation());
            assertNull(state.getPausedUntil());
        }
    }

    @Nested
    @DisplayName("updateLastKnownLocation()")
    class UpdateLastKnownLocationTests {

        @Test
        @DisplayName("should update last known location id")
        void shouldUpdateLastKnownLocationId() {
            LocationSharingState state = LocationSharingState.create(UUID.randomUUID(), UUID.randomUUID());
            UUID locationId = UUID.randomUUID();

            state.updateLastKnownLocation(locationId);

            assertEquals(locationId, state.getLastKnownLocationId());
        }
    }
}
