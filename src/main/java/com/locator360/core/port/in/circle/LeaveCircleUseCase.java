package com.locator360.core.port.in.circle;

import java.util.UUID;

public interface LeaveCircleUseCase {

    void execute(UUID userId, UUID circleId);
}
