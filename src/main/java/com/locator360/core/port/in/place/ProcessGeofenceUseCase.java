package com.locator360.core.port.in.place;

import com.locator360.core.domain.location.Location;

public interface ProcessGeofenceUseCase {

    void execute(Location location);
}
