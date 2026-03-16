package com.locator360.core.port.out;

import com.locator360.core.domain.location.Location;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LocationRepository {

    List<Location> saveAll(List<Location> locations);

    List<Location> findByUserIdAndRecordedAtBetween(UUID userId, Instant start, Instant end);
}
