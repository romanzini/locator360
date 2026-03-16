package com.locator360.infrastructure.event.kafka.publisher;

import com.locator360.core.domain.location.Location;
import com.locator360.core.port.out.LocationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaLocationEventPublisher implements LocationEventPublisher {

    static final String TOPIC = "location.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(List<Location> locations) {
        log.debug("Publishing {} location events to Kafka", locations.size());
        for (Location location : locations) {
            String partitionKey = location.getUserId().toString();
            kafkaTemplate.send(TOPIC, partitionKey, location);
        }
        log.info("Published {} location events to Kafka", locations.size());
    }
}
