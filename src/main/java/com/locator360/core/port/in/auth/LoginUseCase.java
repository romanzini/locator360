package com.locator360.core.port.in.auth;

import com.locator360.core.port.in.dto.input.LoginWithEmailInputDto;
import com.locator360.core.port.in.dto.input.LoginWithPhoneInputDto;
import com.locator360.core.port.in.dto.output.LoginOutputDto;

public interface LoginUseCase {

  LoginOutputDto loginWithEmail(LoginWithEmailInputDto input);

  LoginOutputDto loginWithPhone(LoginWithPhoneInputDto input);
}
