package com.locator360.core.port.out;

import com.locator360.core.domain.location.LocationSharingState;

import java.util.Optional;
import java.util.UUID;

public interface LocationSharingStateRepository {

    LocationSharingState save(LocationSharingState state);

    Optional<LocationSharingState> findByUserIdAndCircleId(UUID userId, UUID circleId);
}
