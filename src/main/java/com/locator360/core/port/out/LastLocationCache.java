package com.locator360.core.port.out;

import com.locator360.core.domain.location.Location;

import java.util.Optional;
import java.util.UUID;

public interface LastLocationCache {

    void save(UUID userId, Location location);

    Optional<Location> findByUserId(UUID userId);
}
