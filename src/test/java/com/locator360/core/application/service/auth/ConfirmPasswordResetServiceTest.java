package com.locator360.core.application.service.auth;

import com.locator360.core.domain.user.AuthIdentity;
import com.locator360.core.domain.user.AuthProvider;
import com.locator360.core.domain.user.TokenType;
import com.locator360.core.domain.user.VerificationToken;
import com.locator360.core.port.in.dto.input.ConfirmPasswordResetInputDto;
import com.locator360.core.port.out.AuthIdentityRepository;
import com.locator360.core.port.out.PasswordEncoder;
import com.locator360.core.port.out.VerificationTokenRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmPasswordResetServiceTest {

  @Mock
  private VerificationTokenRepository verificationTokenRepository;

  @Mock
  private AuthIdentityRepository authIdentityRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private MeterRegistry meterRegistry;

  @Mock
  private Counter counter;

  @InjectMocks
  private ConfirmPasswordResetService confirmPasswordResetService;

  @Nested
  @DisplayName("execute")
  class ExecuteTests {

    private VerificationToken validToken;
    private AuthIdentity passwordIdentity;

    @BeforeEach
    void setUp() {
      UUID userId = UUID.randomUUID();
      Instant now = Instant.now();

      validToken = VerificationToken.restore(
          UUID.randomUUID(), userId, TokenType.PASSWORD_RESET,
          "reset-token-123", now.plus(24, ChronoUnit.HOURS),
          null, now);

      passwordIdentity = AuthIdentity.restore(
          UUID.randomUUID(), userId, AuthProvider.PASSWORD,
          null, "maria@example.com", null, "old_hash",
          true, null, now, now);

      lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
      lenient().when(authIdentityRepository.save(any(AuthIdentity.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
      lenient().when(verificationTokenRepository.save(any(VerificationToken.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("should update password and mark token as used")
    void shouldUpdatePasswordAndMarkTokenAsUsed() {
      ConfirmPasswordResetInputDto input = new ConfirmPasswordResetInputDto("reset-token-123", "NovaSenha123!");

      when(verificationTokenRepository.findByToken("reset-token-123"))
          .thenReturn(Optional.of(validToken));
      when(authIdentityRepository.findByUserIdAndProvider(validToken.getUserId(), AuthProvider.PASSWORD))
          .thenReturn(Optional.of(passwordIdentity));
      when(passwordEncoder.encode("NovaSenha123!")).thenReturn("new_hash");

      confirmPasswordResetService.execute(input);

      ArgumentCaptor<AuthIdentity> identityCaptor = ArgumentCaptor.forClass(AuthIdentity.class);
      verify(authIdentityRepository).save(identityCaptor.capture());
      assertEquals("new_hash", identityCaptor.getValue().getPasswordHash());

      ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
      verify(verificationTokenRepository).save(tokenCaptor.capture());
      assertNotNull(tokenCaptor.getValue().getUsedAt());

      verify(meterRegistry).counter("users.password_reset.confirm", "status", "success");
      verify(counter).increment();
    }

    @Test
    @DisplayName("should throw when token is not found")
    void shouldThrowWhenTokenIsNotFound() {
      ConfirmPasswordResetInputDto input = new ConfirmPasswordResetInputDto("missing-token", "NovaSenha123!");

      when(verificationTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> confirmPasswordResetService.execute(input));

      assertEquals("Invalid password reset token", exception.getMessage());
    }

    @Test
    @DisplayName("should throw when token is expired")
    void shouldThrowWhenTokenIsExpired() {
      VerificationToken expiredToken = VerificationToken.restore(
          validToken.getId(), validToken.getUserId(), TokenType.PASSWORD_RESET,
          validToken.getToken(), Instant.now().minus(1, ChronoUnit.MINUTES),
          null, Instant.now().minus(2, ChronoUnit.HOURS));
      ConfirmPasswordResetInputDto input = new ConfirmPasswordResetInputDto("reset-token-123", "NovaSenha123!");

      when(verificationTokenRepository.findByToken("reset-token-123"))
          .thenReturn(Optional.of(expiredToken));

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> confirmPasswordResetService.execute(input));

      assertEquals("Invalid or expired password reset token", exception.getMessage());
    }

    @Test
    @DisplayName("should throw when token type is not password reset")
    void shouldThrowWhenTokenTypeIsInvalid() {
      VerificationToken invalidTypeToken = VerificationToken.restore(
          validToken.getId(), validToken.getUserId(), TokenType.EMAIL_VERIFICATION,
          validToken.getToken(), validToken.getExpiresAt(),
          null, validToken.getCreatedAt());
      ConfirmPasswordResetInputDto input = new ConfirmPasswordResetInputDto("reset-token-123", "NovaSenha123!");

      when(verificationTokenRepository.findByToken("reset-token-123"))
          .thenReturn(Optional.of(invalidTypeToken));

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> confirmPasswordResetService.execute(input));

      assertEquals("Invalid or expired password reset token", exception.getMessage());
    }

    @Test
    @DisplayName("should throw when password identity is not found")
    void shouldThrowWhenPasswordIdentityIsNotFound() {
      ConfirmPasswordResetInputDto input = new ConfirmPasswordResetInputDto("reset-token-123", "NovaSenha123!");

      when(verificationTokenRepository.findByToken("reset-token-123"))
          .thenReturn(Optional.of(validToken));
      when(authIdentityRepository.findByUserIdAndProvider(validToken.getUserId(), AuthProvider.PASSWORD))
          .thenReturn(Optional.empty());

      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> confirmPasswordResetService.execute(input));

      assertEquals("Password identity not found", exception.getMessage());
    }
  }
}