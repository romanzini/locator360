package com.locator360.core.port.out;

import com.locator360.core.domain.circle.Circle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CircleRepository {

    Circle save(Circle circle);

    Optional<Circle> findById(UUID id);

    List<Circle> findByCreatedByUserId(UUID userId);
}
