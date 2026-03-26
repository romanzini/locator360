package com.locator360.core.port.out;

import com.locator360.core.domain.place.PlaceEvent;

import java.util.Optional;
import java.util.UUID;

public interface PlaceEventRepository {

    PlaceEvent save(PlaceEvent event);

    Optional<PlaceEvent> findLastByPlaceIdAndUserId(UUID placeId, UUID userId);
}
