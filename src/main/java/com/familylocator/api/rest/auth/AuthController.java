package com.familylocator.api.rest.auth;

import com.familylocator.core.port.in.auth.RegisterUserUseCase;
import com.familylocator.core.port.in.dto.input.RegisterWithEmailInputDto;
import com.familylocator.core.port.in.dto.input.RegisterWithPhoneInputDto;
import com.familylocator.core.port.in.dto.output.RegisterUserOutputDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthControllerApi {

  private final RegisterUserUseCase registerUserUseCase;

  @Override
  public ResponseEntity<RegisterUserOutputDto> registerWithEmail(
      @Valid @RequestBody RegisterWithEmailInputDto input) {
    log.debug("Received register request with email: {}", input.getEmail());
    RegisterUserOutputDto output = registerUserUseCase.registerWithEmail(input);
    log.info("User registered with email: {}", output.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(output);
  }

  @Override
  public ResponseEntity<RegisterUserOutputDto> registerWithPhone(
      @Valid @RequestBody RegisterWithPhoneInputDto input) {
    log.debug("Received register request with phone: {}", input.getPhoneNumber());
    RegisterUserOutputDto output = registerUserUseCase.registerWithPhone(input);
    log.info("User registered with phone: {}", output.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(output);
  }
}
