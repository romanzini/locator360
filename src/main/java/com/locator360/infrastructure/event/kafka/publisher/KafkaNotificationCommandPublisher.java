package com.locator360.infrastructure.event.kafka.publisher;

import com.locator360.core.domain.notification.NotificationCommand;
import com.locator360.core.port.out.NotificationCommandPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationCommandPublisher implements NotificationCommandPublisher {

    static final String TOPIC = "notification.commands";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(NotificationCommand command) {
        String partitionKey = command.getCircleId() != null
                ? command.getCircleId().toString()
                : command.getRecipientUserId().toString();

        log.debug("Publishing {} notification to user: {} for circle: {}",
                command.getType(), command.getRecipientUserId(), command.getCircleId());

        kafkaTemplate.send(TOPIC, partitionKey, command);

        log.info("Published {} notification to user: {}", command.getType(), command.getRecipientUserId());
    }
}
