package com.locator360.core.port.in.auth;

import com.locator360.core.port.in.dto.input.ConfirmPasswordResetInputDto;

public interface ConfirmPasswordResetUseCase {

  void execute(ConfirmPasswordResetInputDto input);
}