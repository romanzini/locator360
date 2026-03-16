package com.locator360.core.port.out;

import com.locator360.core.domain.location.Location;

import java.util.List;

public interface LocationEventPublisher {

    void publish(List<Location> locations);
}
