package com.locator360.api.kafka.geofence;

import com.locator360.core.domain.location.Location;
import com.locator360.core.port.in.place.ProcessGeofenceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeofenceConsumer {

    private final ProcessGeofenceUseCase processGeofenceUseCase;

    @KafkaListener(topics = "location.events", groupId = "geofence-consumer")
    public void consume(Location location) {
        log.info("Received location event for geofence processing, user: {}", location.getUserId());

        try {
            processGeofenceUseCase.execute(location);
            log.info("Geofence processing completed for user: {}", location.getUserId());
        } catch (Exception ex) {
            log.error("Failed to process geofence for user: {}", location.getUserId(), ex);
        }
    }
}
