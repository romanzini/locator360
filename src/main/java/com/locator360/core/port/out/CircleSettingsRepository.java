package com.locator360.core.port.out;

import com.locator360.core.domain.circle.CircleSettings;

import java.util.Optional;
import java.util.UUID;

public interface CircleSettingsRepository {

    CircleSettings save(CircleSettings settings);

    Optional<CircleSettings> findByCircleId(UUID circleId);
}
