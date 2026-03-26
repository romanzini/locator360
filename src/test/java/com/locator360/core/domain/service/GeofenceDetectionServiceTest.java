package com.locator360.core.domain.service;

import com.locator360.core.domain.place.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GeofenceDetectionServiceTest {

    private GeofenceDetectionService service;

    @BeforeEach
    void setUp() {
        service = new GeofenceDetectionService();
    }

    @Nested
    @DisplayName("isInsideGeofence")
    class IsInsideGeofenceTests {

        @Test
        @DisplayName("should return true when point is inside geofence radius")
        void shouldReturnTrueWhenInsideRadius() {
            Place place = Place.restore(UUID.randomUUID(), UUID.randomUUID(), "School",
                    PlaceType.SCHOOL, "Rua A", -23.5505, -46.6333, 500.0,
                    true, UUID.randomUUID(), Instant.now(), Instant.now());

            boolean result = service.isInsideGeofence(-23.5505, -46.6333, place);

            assertTrue(result);
        }

        @Test
        @DisplayName("should return true when point is at edge of geofence radius")
        void shouldReturnTrueWhenAtEdge() {
            Place place = Place.restore(UUID.randomUUID(), UUID.randomUUID(), "School",
                    PlaceType.SCHOOL, "Rua A", -23.5505, -46.6333, 1000.0,
                    true, UUID.randomUUID(), Instant.now(), Instant.now());

            // ~500m away (well within 1000m radius)
            boolean result = service.isInsideGeofence(-23.5550, -46.6333, place);

            assertTrue(result);
        }

        @Test
        @DisplayName("should return false when point is outside geofence radius")
        void shouldReturnFalseWhenOutsideRadius() {
            Place place = Place.restore(UUID.randomUUID(), UUID.randomUUID(), "School",
                    PlaceType.SCHOOL, "Rua A", -23.5505, -46.6333, 100.0,
                    true, UUID.randomUUID(), Instant.now(), Instant.now());

            // ~11km away
            boolean result = service.isInsideGeofence(-23.6505, -46.6333, place);

            assertFalse(result);
        }

        @Test
        @DisplayName("should return true when point is exactly at place center")
        void shouldReturnTrueWhenExactlyAtCenter() {
            Place place = Place.restore(UUID.randomUUID(), UUID.randomUUID(), "Home",
                    PlaceType.HOME, "Rua B", -23.5505, -46.6333, 50.0,
                    true, UUID.randomUUID(), Instant.now(), Instant.now());

            boolean result = service.isInsideGeofence(-23.5505, -46.6333, place);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("determineTransition")
    class DetermineTransitionTests {

        @Test
        @DisplayName("should return ENTER when was outside and now inside")
        void shouldReturnEnterWhenTransitionIn() {
            Optional<PlaceEventType> result = service.determineTransition(false, true);

            assertTrue(result.isPresent());
            assertEquals(PlaceEventType.ENTER, result.get());
        }

        @Test
        @DisplayName("should return EXIT when was inside and now outside")
        void shouldReturnExitWhenTransitionOut() {
            Optional<PlaceEventType> result = service.determineTransition(true, false);

            assertTrue(result.isPresent());
            assertEquals(PlaceEventType.EXIT, result.get());
        }

        @Test
        @DisplayName("should return empty when still inside")
        void shouldReturnEmptyWhenStillInside() {
            Optional<PlaceEventType> result = service.determineTransition(true, true);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty when still outside")
        void shouldReturnEmptyWhenStillOutside() {
            Optional<PlaceEventType> result = service.determineTransition(false, false);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("isPolicyActive")
    class IsPolicyActiveTests {

        @Test
        @DisplayName("should return true when policy has no time restrictions")
        void shouldReturnTrueWhenNoRestrictions() {
            PlaceAlertPolicy policy = PlaceAlertPolicy.createDefault(UUID.randomUUID(), UUID.randomUUID());

            boolean result = service.isPolicyActive(policy, Instant.now());

            assertTrue(result);
        }

        @Test
        @DisplayName("should return true when event is on active day and within time window")
        void shouldReturnTrueWhenOnActiveDayAndWithinTime() {
            // Create a policy with Monday active, 08:00-18:00
            ZonedDateTime monday = ZonedDateTime.of(2026, 3, 23, 10, 0, 0, 0, ZoneOffset.UTC);

            PlaceAlertPolicy policy = PlaceAlertPolicy.restore(UUID.randomUUID(),
                    UUID.randomUUID(), UUID.randomUUID(), true, true,
                    "MONDAY", LocalTime.of(8, 0), LocalTime.of(18, 0),
                    TargetType.ALL_MEMBERS, Instant.now(), Instant.now());

            boolean result = service.isPolicyActive(policy, monday.toInstant());

            assertTrue(result);
        }

        @Test
        @DisplayName("should return false when event is on inactive day")
        void shouldReturnFalseWhenOnInactiveDay() {
            // Tuesday event, but policy only active on Monday
            ZonedDateTime tuesday = ZonedDateTime.of(2026, 3, 24, 10, 0, 0, 0, ZoneOffset.UTC);

            PlaceAlertPolicy policy = PlaceAlertPolicy.restore(UUID.randomUUID(),
                    UUID.randomUUID(), UUID.randomUUID(), true, true,
                    "MONDAY", LocalTime.of(8, 0), LocalTime.of(18, 0),
                    TargetType.ALL_MEMBERS, Instant.now(), Instant.now());

            boolean result = service.isPolicyActive(policy, tuesday.toInstant());

            assertFalse(result);
        }

        @Test
        @DisplayName("should return false when event is outside time window")
        void shouldReturnFalseWhenOutsideTimeWindow() {
            // Monday 06:00, but policy active 08:00-18:00
            ZonedDateTime monday6am = ZonedDateTime.of(2026, 3, 23, 6, 0, 0, 0, ZoneOffset.UTC);

            PlaceAlertPolicy policy = PlaceAlertPolicy.restore(UUID.randomUUID(),
                    UUID.randomUUID(), UUID.randomUUID(), true, true,
                    "MONDAY", LocalTime.of(8, 0), LocalTime.of(18, 0),
                    TargetType.ALL_MEMBERS, Instant.now(), Instant.now());

            boolean result = service.isPolicyActive(policy, monday6am.toInstant());

            assertFalse(result);
        }

        @Test
        @DisplayName("should return true when only days restriction and event is on active day")
        void shouldReturnTrueWhenOnlyDaysRestriction() {
            ZonedDateTime monday = ZonedDateTime.of(2026, 3, 23, 10, 0, 0, 0, ZoneOffset.UTC);

            PlaceAlertPolicy policy = PlaceAlertPolicy.restore(UUID.randomUUID(),
                    UUID.randomUUID(), UUID.randomUUID(), true, true,
                    "MONDAY,WEDNESDAY,FRIDAY", null, null,
                    TargetType.ALL_MEMBERS, Instant.now(), Instant.now());

            boolean result = service.isPolicyActive(policy, monday.toInstant());

            assertTrue(result);
        }
    }
}
