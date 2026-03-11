package com.locator360.core.port.out;

import com.locator360.core.domain.notification.NotificationCommand;

public interface NotificationCommandPublisher {

    void publish(NotificationCommand command);
}
