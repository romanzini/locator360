package com.locator360.core.port.in.location;

import java.util.UUID;

public interface ResumeLocationSharingUseCase {

  void execute(UUID userId, UUID circleId);
}
