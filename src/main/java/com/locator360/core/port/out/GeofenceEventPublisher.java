package com.locator360.core.port.out;

import com.locator360.core.domain.place.PlaceEvent;

public interface GeofenceEventPublisher {

    void publish(PlaceEvent event);
}
