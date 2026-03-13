package com.locator360.core.port.in.user;

import java.util.UUID;

public interface RevokeDeviceUseCase {

    void execute(UUID userId, UUID deviceId);
}
