package com.locator360.core.port.in.circle;

import java.util.UUID;

public interface RemoveMemberUseCase {

    void execute(UUID adminId, UUID circleId, UUID memberId);
}
