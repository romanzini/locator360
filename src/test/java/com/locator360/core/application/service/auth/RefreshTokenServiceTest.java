package com.locator360.core.application.service.auth;

import com.locator360.core.domain.user.DistanceUnit;
import com.locator360.core.domain.user.User;
import com.locator360.core.domain.user.UserStatus;
import com.locator360.core.port.in.dto.input.RefreshTokenInputDto;
import com.locator360.core.port.in.dto.output.LoginOutputDto;
import com.locator360.core.port.out.TokenProvider;
import com.locator360.core.port.out.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock
  private TokenProvider tokenProvider;

  @Mock
  private UserRepository userRepository;

  @Mock
  private MeterRegistry meterRegistry;

  @Mock
  private Counter counter;

  @InjectMocks
  private RefreshTokenService refreshTokenService;

  @Nested
  @DisplayName("execute")
  class ExecuteTests {

    private RefreshTokenInputDto validInput;
    private User activeUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
      validInput = new RefreshTokenInputDto("valid_refresh_token");
      userId = UUID.randomUUID();

      activeUser = User.restore(
          userId, "maria@example.com", null,
          "Maria Oliveira", "Maria", "Oliveira",
          null, null, null, "pt-BR", "America/Sao_Paulo",
          DistanceUnit.KM, UserStatus.ACTIVE, Instant.now(), Instant.now());

      lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
    }

    @Test
    @DisplayName("should refresh tokens successfully")
    void shouldRefreshTokensSuccessfully() {
      when(tokenProvider.validateToken("valid_refresh_token")).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
      when(tokenProvider.generateAccessToken(userId)).thenReturn("new_access_token");
      when(tokenProvider.generateRefreshToken(userId)).thenReturn("new_refresh_token");
      when(tokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);

      LoginOutputDto result = refreshTokenService.execute(validInput);

      assertNotNull(result);
      assertEquals("new_access_token", result.getAccessToken());
      assertEquals("new_refresh_token", result.getRefreshToken());
      assertEquals("Bearer", result.getTokenType());
      assertEquals(3600, result.getExpiresIn());
      assertNotNull(result.getUser());
      assertEquals(userId, result.getUser().getId());
      assertEquals("maria@example.com", result.getUser().getEmail());
    }

    @Test
    @DisplayName("should throw when refresh token is invalid")
    void shouldThrowWhenTokenInvalid() {
      when(tokenProvider.validateToken("valid_refresh_token"))
          .thenThrow(new IllegalArgumentException("Invalid or expired token"));

      IllegalArgumentException ex = assertThrows(
          IllegalArgumentException.class,
          () -> refreshTokenService.execute(validInput));

      assertEquals("Invalid or expired token", ex.getMessage());
      verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("should throw when user not found for token")
    void shouldThrowWhenUserNotFound() {
      when(tokenProvider.validateToken("valid_refresh_token")).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      IllegalArgumentException ex = assertThrows(
          IllegalArgumentException.class,
          () -> refreshTokenService.execute(validInput));

      assertEquals("User not found", ex.getMessage());
    }

    @Test
    @DisplayName("should throw when user account is blocked")
    void shouldThrowWhenAccountBlocked() {
      User blockedUser = User.restore(
          userId, "maria@example.com", null,
          "Maria Oliveira", "Maria", "Oliveira",
          null, null, null, "pt-BR", "America/Sao_Paulo",
          DistanceUnit.KM, UserStatus.BLOCKED, Instant.now(), Instant.now());

      when(tokenProvider.validateToken("valid_refresh_token")).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(blockedUser));

      IllegalStateException ex = assertThrows(
          IllegalStateException.class,
          () -> refreshTokenService.execute(validInput));

      assertEquals("Account is blocked", ex.getMessage());
    }
  }
}
