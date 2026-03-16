package com.locator360.core.port.in.location;

import com.locator360.core.port.in.dto.input.PauseLocationInputDto;

import java.util.UUID;

public interface PauseLocationSharingUseCase {

  void execute(UUID userId, UUID circleId, PauseLocationInputDto input);
}
