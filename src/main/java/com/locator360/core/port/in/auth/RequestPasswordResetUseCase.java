package com.locator360.core.port.in.auth;

import com.locator360.core.port.in.dto.input.RequestPasswordResetInputDto;

public interface RequestPasswordResetUseCase {

  void execute(RequestPasswordResetInputDto input);
}