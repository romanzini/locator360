package com.locator360.infrastructure.event.kafka.publisher;

import com.locator360.core.domain.place.PlaceEvent;
import com.locator360.core.port.out.GeofenceEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaGeofenceEventPublisher implements GeofenceEventPublisher {

    static final String TOPIC = "geofence.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(PlaceEvent event) {
        String partitionKey = event.getCircleId().toString();

        log.debug("Publishing geofence {} event for user: {} at place: {}",
                event.getEventType(), event.getUserId(), event.getPlaceId());

        kafkaTemplate.send(TOPIC, partitionKey, event);

        log.info("Published geofence {} event for user: {} at place: {}",
                event.getEventType(), event.getUserId(), event.getPlaceId());
    }
}
