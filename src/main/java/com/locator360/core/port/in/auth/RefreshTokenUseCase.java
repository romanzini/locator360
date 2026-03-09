package com.locator360.core.port.in.auth;

import com.locator360.core.port.in.dto.input.RefreshTokenInputDto;
import com.locator360.core.port.in.dto.output.LoginOutputDto;

public interface RefreshTokenUseCase {

  LoginOutputDto execute(RefreshTokenInputDto input);
}
