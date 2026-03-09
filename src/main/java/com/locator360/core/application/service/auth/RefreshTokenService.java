package com.locator360.core.application.service.auth;

import com.locator360.core.domain.user.User;
import com.locator360.core.domain.user.UserStatus;
import com.locator360.core.port.in.auth.RefreshTokenUseCase;
import com.locator360.core.port.in.dto.input.RefreshTokenInputDto;
import com.locator360.core.port.in.dto.output.LoginOutputDto;
import com.locator360.core.port.out.TokenProvider;
import com.locator360.core.port.out.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RefreshTokenService implements RefreshTokenUseCase {

  private final TokenProvider tokenProvider;
  private final UserRepository userRepository;
  private final MeterRegistry meterRegistry;

  @Override
  public LoginOutputDto execute(RefreshTokenInputDto input) {
    log.debug("Refresh token attempt");

    UUID userId = tokenProvider.validateToken(input.getRefreshToken());

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.getStatus() == UserStatus.BLOCKED) {
      throw new IllegalStateException("Account is blocked");
    }

    String newAccessToken = tokenProvider.generateAccessToken(userId);
    String newRefreshToken = tokenProvider.generateRefreshToken(userId);

    log.info("Token refreshed for user: {}", userId);
    meterRegistry.counter("users.token.refresh", "status", "success").increment();

    return LoginOutputDto.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .tokenType("Bearer")
        .expiresIn((int) tokenProvider.getAccessTokenExpirationSeconds())
        .user(LoginOutputDto.UserInfo.builder()
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
            .build())
        .build();
  }
}
