package com.locator360.api.kafka.geofence;

import com.locator360.core.domain.location.Location;
import com.locator360.core.domain.location.LocationSource;
import com.locator360.core.port.in.place.ProcessGeofenceUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeofenceConsumerTest {

    @Mock
    private ProcessGeofenceUseCase processGeofenceUseCase;

    @InjectMocks
    private GeofenceConsumer geofenceConsumer;

    @Test
    @DisplayName("should delegate location event to ProcessGeofenceUseCase")
    void shouldDelegateToUseCase() {
        Location location = Location.create(UUID.randomUUID(), UUID.randomUUID(),
                -23.5505, -46.6333, 10.0, 0.0, null, null,
                LocationSource.GPS, Instant.now(), false, 80);

        geofenceConsumer.consume(location);

        verify(processGeofenceUseCase).execute(location);
    }

    @Test
    @DisplayName("should not throw when use case fails")
    void shouldNotThrowWhenUseCaseFails() {
        Location location = Location.create(UUID.randomUUID(), UUID.randomUUID(),
                -23.5505, -46.6333, 10.0, 0.0, null, null,
                LocationSource.GPS, Instant.now(), false, 80);
        doThrow(new RuntimeException("DB error")).when(processGeofenceUseCase).execute(location);

        geofenceConsumer.consume(location);

        verify(processGeofenceUseCase).execute(location);
    }
}
