package com.familylocator.core.port.in.auth;

import com.familylocator.core.port.in.dto.input.RegisterWithEmailInputDto;
import com.familylocator.core.port.in.dto.input.RegisterWithPhoneInputDto;
import com.familylocator.core.port.in.dto.output.RegisterUserOutputDto;

public interface RegisterUserUseCase {

  RegisterUserOutputDto registerWithEmail(RegisterWithEmailInputDto input);

  RegisterUserOutputDto registerWithPhone(RegisterWithPhoneInputDto input);
}
