package com.locator360.core.application.service.location;

import com.locator360.core.domain.location.Location;
import com.locator360.core.domain.location.LocationSource;
import com.locator360.core.port.in.dto.input.LocationPointDto;
import com.locator360.core.port.in.dto.input.StreamLocationInputDto;
import com.locator360.core.port.out.LastLocationCache;
import com.locator360.core.port.out.LocationEventPublisher;
import com.locator360.core.port.out.LocationRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreamLocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationEventPublisher locationEventPublisher;

    @Mock
    private LastLocationCache lastLocationCache;

    private SimpleMeterRegistry meterRegistry;

    private StreamLocationService streamLocationService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        meterRegistry = new SimpleMeterRegistry();
        streamLocationService = new StreamLocationService(
                locationRepository, locationEventPublisher, lastLocationCache, meterRegistry);
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("should process location events and save to repository")
        void shouldProcessLocationEventsAndSave() {
            StreamLocationInputDto input = createInputWithEvents(2);
            when(locationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            streamLocationService.execute(userId, input);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Location>> captor = ArgumentCaptor.forClass(List.class);
            verify(locationRepository).saveAll(captor.capture());

            List<Location> savedLocations = captor.getValue();
            assertEquals(2, savedLocations.size());
            assertEquals(userId, savedLocations.get(0).getUserId());
            assertEquals(-23.561414, savedLocations.get(0).getLatitude());
            assertEquals(LocationSource.GPS, savedLocations.get(0).getSource());
        }

        @Test
        @DisplayName("should publish events to Kafka")
        void shouldPublishEventsToKafka() {
            StreamLocationInputDto input = createInputWithEvents(3);
            when(locationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            streamLocationService.execute(userId, input);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Location>> captor = ArgumentCaptor.forClass(List.class);
            verify(locationEventPublisher).publish(captor.capture());

            assertEquals(3, captor.getValue().size());
        }

        @Test
        @DisplayName("should update last location cache with most recent event")
        void shouldUpdateCacheWithMostRecentEvent() {
            Instant older = Instant.now().minus(10, ChronoUnit.MINUTES);
            Instant newer = Instant.now();

            LocationPointDto olderPoint = createPointDto(-23.561414, -46.655881, older);
            LocationPointDto newerPoint = createPointDto(-23.562000, -46.656000, newer);

            StreamLocationInputDto input = new StreamLocationInputDto(null, List.of(olderPoint, newerPoint));
            when(locationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            streamLocationService.execute(userId, input);

            ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
            verify(lastLocationCache).save(eq(userId), captor.capture());

            assertEquals(newer, captor.getValue().getRecordedAt());
        }

        @Test
        @DisplayName("should pass circleId to created locations")
        void shouldPassCircleIdToLocations() {
            UUID circleId = UUID.randomUUID();
            LocationPointDto point = createPointDto(-23.561414, -46.655881, Instant.now());
            StreamLocationInputDto input = new StreamLocationInputDto(circleId, List.of(point));
            when(locationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            streamLocationService.execute(userId, input);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Location>> captor = ArgumentCaptor.forClass(List.class);
            verify(locationRepository).saveAll(captor.capture());

            assertEquals(circleId, captor.getValue().get(0).getCircleId());
        }

        @Test
        @DisplayName("should handle null circleId")
        void shouldHandleNullCircleId() {
            LocationPointDto point = createPointDto(-23.561414, -46.655881, Instant.now());
            StreamLocationInputDto input = new StreamLocationInputDto(null, List.of(point));
            when(locationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            streamLocationService.execute(userId, input);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Location>> captor = ArgumentCaptor.forClass(List.class);
            verify(locationRepository).saveAll(captor.capture());

            assertNull(captor.getValue().get(0).getCircleId());
        }

        @Test
        @DisplayName("should increment locations.ingested counter with batch size")
        void shouldIncrementMetricsCounter() {
            StreamLocationInputDto input = createInputWithEvents(5);
            when(locationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            streamLocationService.execute(userId, input);

            double count = meterRegistry.counter("locations.ingested").count();
            assertEquals(5.0, count);
        }

        @Test
        @DisplayName("should map isMoving correctly")
        void shouldMapIsMovingCorrectly() {
            LocationPointDto point = new LocationPointDto(
                    -23.561414, -46.655881, 10.5, 5.0, 180.0, 760.0,
                    "GPS", Instant.now(), true, 72);
            StreamLocationInputDto input = new StreamLocationInputDto(null, List.of(point));
            when(locationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            streamLocationService.execute(userId, input);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Location>> captor = ArgumentCaptor.forClass(List.class);
            verify(locationRepository).saveAll(captor.capture());

            assertTrue(captor.getValue().get(0).isMoving());
        }

        @Test
        @DisplayName("should default isMoving to false when null")
        void shouldDefaultIsMovingToFalseWhenNull() {
            LocationPointDto point = new LocationPointDto(
                    -23.561414, -46.655881, null, null, null, null,
                    "GPS", Instant.now(), null, null);
            StreamLocationInputDto input = new StreamLocationInputDto(null, List.of(point));
            when(locationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            streamLocationService.execute(userId, input);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Location>> captor = ArgumentCaptor.forClass(List.class);
            verify(locationRepository).saveAll(captor.capture());

            assertFalse(captor.getValue().get(0).isMoving());
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────

    private StreamLocationInputDto createInputWithEvents(int count) {
        List<LocationPointDto> events = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            events.add(createPointDto(
                    -23.561414 + (i * 0.001),
                    -46.655881 + (i * 0.001),
                    Instant.now().minus(count - i, ChronoUnit.MINUTES)));
        }
        return new StreamLocationInputDto(null, events);
    }

    private LocationPointDto createPointDto(double lat, double lon, Instant recordedAt) {
        return new LocationPointDto(lat, lon, 10.5, 5.0, 180.0, 760.0,
                "GPS", recordedAt, false, 72);
    }
}
