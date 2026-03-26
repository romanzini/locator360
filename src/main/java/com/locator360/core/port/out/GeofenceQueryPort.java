package com.locator360.core.port.out;

import com.locator360.core.domain.place.Place;

import java.util.List;
import java.util.UUID;

public interface GeofenceQueryPort {

    List<Place> findPlacesNearPoint(double latitude, double longitude, List<UUID> circleIds);
}
