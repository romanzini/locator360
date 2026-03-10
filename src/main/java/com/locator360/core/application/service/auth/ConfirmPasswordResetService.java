package com.locator360.core.application.service.auth;

import com.locator360.core.domain.user.AuthIdentity;
import com.locator360.core.domain.user.AuthProvider;
import com.locator360.core.domain.user.TokenType;
import com.locator360.core.domain.user.VerificationToken;
import com.locator360.core.port.in.auth.ConfirmPasswordResetUseCase;
import com.locator360.core.port.in.dto.input.ConfirmPasswordResetInputDto;
import com.locator360.core.port.out.AuthIdentityRepository;
import com.locator360.core.port.out.PasswordEncoder;
import com.locator360.core.port.out.VerificationTokenRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConfirmPasswordResetService implements ConfirmPasswordResetUseCase {

  private final VerificationTokenRepository verificationTokenRepository;
  private final AuthIdentityRepository authIdentityRepository;
  private final PasswordEncoder passwordEncoder;
  private final MeterRegistry meterRegistry;

  @Override
  public void execute(ConfirmPasswordResetInputDto input) {
    log.debug("Password reset confirmation received");

    VerificationToken token = verificationTokenRepository.findByToken(input.getToken())
        .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

    if (token.getType() != TokenType.PASSWORD_RESET || !token.isValid()) {
      throw new IllegalArgumentException("Invalid or expired password reset token");
    }

    AuthIdentity identity = authIdentityRepository
        .findByUserIdAndProvider(token.getUserId(), AuthProvider.PASSWORD)
        .orElseThrow(() -> new IllegalArgumentException("Password identity not found"));

    String passwordHash = passwordEncoder.encode(input.getNewPassword());
    identity.updatePassword(passwordHash);
    authIdentityRepository.save(identity);

    token.markUsed();
    verificationTokenRepository.save(token);

    log.info("Password reset completed for user: {}", token.getUserId());
    meterRegistry.counter("users.password_reset.confirm", "status", "success").increment();
  }
}