package com.locator360.core.application.service.auth;

import com.locator360.core.domain.user.AuthProvider;
import com.locator360.core.domain.user.TokenType;
import com.locator360.core.domain.user.User;
import com.locator360.core.domain.user.VerificationToken;
import com.locator360.core.port.in.auth.RequestPasswordResetUseCase;
import com.locator360.core.port.in.dto.input.RequestPasswordResetInputDto;
import com.locator360.core.port.out.AuthIdentityRepository;
import com.locator360.core.port.out.NotificationSender;
import com.locator360.core.port.out.UserRepository;
import com.locator360.core.port.out.VerificationTokenRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

  private final UserRepository userRepository;
  private final AuthIdentityRepository authIdentityRepository;
  private final VerificationTokenRepository verificationTokenRepository;
  private final NotificationSender notificationSender;
  private final MeterRegistry meterRegistry;

  @Override
  public void execute(RequestPasswordResetInputDto input) {
    String channel = input.hasEmail() ? "email" : "sms";
    log.debug("Password reset request received via {}", channel);

    Optional<User> userOptional = input.hasEmail()
        ? userRepository.findByEmail(input.getEmail())
        : userRepository.findByPhoneNumber(input.getPhoneNumber());

    if (userOptional.isEmpty()) {
      log.info("Password reset request ignored for unknown {}", channel);
      return;
    }

    User user = userOptional.get();
    boolean hasPasswordIdentity = authIdentityRepository
        .findByUserIdAndProvider(user.getId(), AuthProvider.PASSWORD)
        .isPresent();

    if (!hasPasswordIdentity) {
      log.info("Password reset request ignored for user without password identity: {}", user.getId());
      return;
    }

    VerificationToken token = VerificationToken.create(user.getId(), TokenType.PASSWORD_RESET);
    verificationTokenRepository.save(token);

    if (input.hasEmail()) {
      notificationSender.sendEmail(
          input.getEmail(),
          "Password reset instructions",
          "Use this token to reset your password: " + token.getToken());
    } else {
      notificationSender.sendSms(
          input.getPhoneNumber(),
          "Password reset token: " + token.getToken());
    }

    log.info("Password reset token generated for user: {}", user.getId());
    meterRegistry.counter("users.password_reset.request", "channel", channel, "status", "sent")
        .increment();
  }
}