package com.locator360.api.rest.auth;

import com.locator360.core.port.in.auth.LoginUseCase;
import com.locator360.core.port.in.auth.LogoutUseCase;
import com.locator360.core.port.in.auth.ConfirmPasswordResetUseCase;
import com.locator360.core.port.in.auth.RefreshTokenUseCase;
import com.locator360.core.port.in.auth.RequestPasswordResetUseCase;
import com.locator360.core.port.in.auth.RegisterUserUseCase;
import com.locator360.core.port.in.dto.input.ConfirmPasswordResetInputDto;
import com.locator360.core.port.in.dto.input.LoginWithEmailInputDto;
import com.locator360.core.port.in.dto.input.LoginWithPhoneInputDto;
import com.locator360.core.port.in.dto.input.RequestPasswordResetInputDto;
import com.locator360.core.port.in.dto.input.RefreshTokenInputDto;
import com.locator360.core.port.in.dto.input.RegisterWithEmailInputDto;
import com.locator360.core.port.in.dto.input.RegisterWithPhoneInputDto;
import com.locator360.core.port.in.dto.output.LoginOutputDto;
import com.locator360.core.port.in.dto.output.RegisterUserOutputDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthControllerApi {

  private final RegisterUserUseCase registerUserUseCase;
  private final LoginUseCase loginUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final LogoutUseCase logoutUseCase;
  private final RequestPasswordResetUseCase requestPasswordResetUseCase;
  private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;

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

  @Override
  public ResponseEntity<LoginOutputDto> loginWithEmail(
      @Valid @RequestBody LoginWithEmailInputDto input) {
    log.debug("Received login request with email: {}", input.getEmail());
    LoginOutputDto output = loginUseCase.loginWithEmail(input);
    log.info("User logged in with email: {}", output.getUser().getId());
    return ResponseEntity.ok(output);
  }

  @Override
  public ResponseEntity<LoginOutputDto> loginWithPhone(
      @Valid @RequestBody LoginWithPhoneInputDto input) {
    log.debug("Received login request with phone: {}", input.getPhoneNumber());
    LoginOutputDto output = loginUseCase.loginWithPhone(input);
    log.info("User logged in with phone: {}", output.getUser().getId());
    return ResponseEntity.ok(output);
  }

  @Override
  public ResponseEntity<LoginOutputDto> refreshToken(
      @Valid @RequestBody RefreshTokenInputDto input) {
    log.debug("Received refresh token request");
    LoginOutputDto output = refreshTokenUseCase.execute(input);
    log.info("Token refreshed for user: {}", output.getUser().getId());
    return ResponseEntity.ok(output);
  }

  @Override
  public ResponseEntity<Void> logout() {
    UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.debug("Received logout request for user: {}", userId);
    logoutUseCase.execute(userId);
    log.info("User logged out: {}", userId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> requestPasswordReset(
      @Valid @RequestBody RequestPasswordResetInputDto input) {
    log.debug("Received password reset request via {}", input.hasEmail() ? "email" : "sms");
    requestPasswordResetUseCase.execute(input);
    log.info("Password reset request accepted");
    return ResponseEntity.accepted().build();
  }

  @Override
  public ResponseEntity<Void> confirmPasswordReset(
      @Valid @RequestBody ConfirmPasswordResetInputDto input) {
    log.debug("Received password reset confirmation");
    confirmPasswordResetUseCase.execute(input);
    log.info("Password reset confirmed successfully");
    return ResponseEntity.ok().build();
  }
}
