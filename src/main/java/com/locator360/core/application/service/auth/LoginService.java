package com.locator360.core.application.service.auth;

import com.locator360.core.domain.service.AuthenticationService;
import com.locator360.core.domain.user.AuthIdentity;
import com.locator360.core.domain.user.AuthProvider;
import com.locator360.core.domain.user.User;
import com.locator360.core.port.in.auth.LoginUseCase;
import com.locator360.core.port.in.dto.input.LoginWithEmailInputDto;
import com.locator360.core.port.in.dto.input.LoginWithPhoneInputDto;
import com.locator360.core.port.in.dto.output.LoginOutputDto;
import com.locator360.core.port.out.AuthIdentityRepository;
import com.locator360.core.port.out.PasswordEncoder;
import com.locator360.core.port.out.TokenProvider;
import com.locator360.core.port.out.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LoginService implements LoginUseCase {

  private final UserRepository userRepository;
  private final AuthIdentityRepository authIdentityRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenProvider tokenProvider;
  private final AuthenticationService authenticationService;
  private final MeterRegistry meterRegistry;

  @Override
  public LoginOutputDto loginWithEmail(LoginWithEmailInputDto input) {
    log.debug("Login attempt with email: {}", input.getEmail());

    User user = userRepository.findByEmail(input.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    AuthIdentity identity = authIdentityRepository
        .findByUserIdAndProvider(user.getId(), AuthProvider.PASSWORD)
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    authenticationService.validateAccountStatus(user);
    authenticationService.validatePasswordCredentials(identity, input.getPassword(), passwordEncoder);

    identity.recordLogin();
    authIdentityRepository.save(identity);

    LoginOutputDto result = generateLoginOutput(user);

    log.info("User logged in with email: {}", user.getId());
    meterRegistry.counter("users.login", "method", "email", "status", "success").increment();
    return result;
  }

  @Override
  public LoginOutputDto loginWithPhone(LoginWithPhoneInputDto input) {
    log.debug("Login attempt with phone: {}", input.getPhoneNumber());

    User user = userRepository.findByPhoneNumber(input.getPhoneNumber())
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    AuthIdentity identity = authIdentityRepository
        .findByUserIdAndProvider(user.getId(), AuthProvider.PHONE_SMS)
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    authenticationService.validateAccountStatus(user);
    authenticationService.validatePhoneCredentials(identity);

    identity.recordLogin();
    authIdentityRepository.save(identity);

    LoginOutputDto result = generateLoginOutput(user);

    log.info("User logged in with phone: {}", user.getId());
    meterRegistry.counter("users.login", "method", "phone", "status", "success").increment();
    return result;
  }

  private LoginOutputDto generateLoginOutput(User user) {
    String accessToken = tokenProvider.generateAccessToken(user.getId());
    String refreshToken = tokenProvider.generateRefreshToken(user.getId());

    return LoginOutputDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn((int) tokenProvider.getAccessTokenExpirationSeconds())
        .user(buildUserInfo(user))
        .build();
  }

  private LoginOutputDto.UserInfo buildUserInfo(User user) {
    return LoginOutputDto.UserInfo.builder()
        .id(user.getId())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .fullName(user.getFullName())
        .preferredLanguage(user.getPreferredLanguage())
        .timezone(user.getTimezone())
        .distanceUnit(user.getDistanceUnit() != null ? user.getDistanceUnit().name() : null)
        .status(user.getStatus().name())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
