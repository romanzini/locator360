package com.locator360.core.port.in.auth;

import com.locator360.core.port.in.dto.input.RegisterWithEmailInputDto;
import com.locator360.core.port.in.dto.input.RegisterWithPhoneInputDto;
import com.locator360.core.port.in.dto.output.RegisterUserOutputDto;

public interface RegisterUserUseCase {

  RegisterUserOutputDto registerWithEmail(RegisterWithEmailInputDto input);

  RegisterUserOutputDto registerWithPhone(RegisterWithPhoneInputDto input);
}
