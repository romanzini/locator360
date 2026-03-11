package com.locator360.core.domain.notification;

import java.util.Map;
import java.util.UUID;

public class NotificationCommand {

    private final NotificationType type;
    private final UUID recipientUserId;
    private final UUID circleId;
    private final Map<String, Object> payload;

    private NotificationCommand(NotificationType type, UUID recipientUserId, UUID circleId,
                                Map<String, Object> payload) {
        this.type = type;
        this.recipientUserId = recipientUserId;
        this.circleId = circleId;
        this.payload = payload;
    }

    public static NotificationCommand create(NotificationType type, UUID recipientUserId,
                                             UUID circleId, Map<String, Object> payload) {
        return new NotificationCommand(type, recipientUserId, circleId, payload);
    }

    public NotificationType getType() {
        return type;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public UUID getCircleId() {
        return circleId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
