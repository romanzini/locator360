package com.locator360.core.port.in.auth;

import java.util.UUID;

public interface LogoutUseCase {

  void execute(UUID userId);
}
