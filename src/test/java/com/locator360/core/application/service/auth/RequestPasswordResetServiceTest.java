package com.locator360.core.application.service.auth;

import com.locator360.core.domain.user.AuthIdentity;
import com.locator360.core.domain.user.AuthProvider;
import com.locator360.core.domain.user.DistanceUnit;
import com.locator360.core.domain.user.TokenType;
import com.locator360.core.domain.user.User;
import com.locator360.core.domain.user.UserStatus;
import com.locator360.core.domain.user.VerificationToken;
import com.locator360.core.port.in.dto.input.RequestPasswordResetInputDto;
import com.locator360.core.port.out.AuthIdentityRepository;
import com.locator360.core.port.out.NotificationSender;
import com.locator360.core.port.out.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private AuthIdentityRepository authIdentityRepository;

  @Mock
  private VerificationTokenRepository verificationTokenRepository;

  @Mock
  private NotificationSender notificationSender;

  @Mock
  private MeterRegistry meterRegistry;

  @Mock
  private Counter counter;

  @InjectMocks
  private RequestPasswordResetService requestPasswordResetService;

  @Nested
  @DisplayName("execute")
  class ExecuteTests {

    private User emailUser;
    private User phoneUser;
    private AuthIdentity passwordIdentity;

    @BeforeEach
    void setUp() {
      emailUser = User.restore(
          UUID.randomUUID(), "maria@example.com", null,
          "Maria Oliveira", "Maria", "Oliveira",
          null, null, null, "pt-BR", "America/Sao_Paulo",
          DistanceUnit.KM, UserStatus.ACTIVE, Instant.now(), Instant.now());

      phoneUser = User.restore(
          UUID.randomUUID(), "maria@example.com", "+5511999999999",
          "Maria Oliveira", "Maria", "Oliveira",
          null, null, null, "pt-BR", "America/Sao_Paulo",
          DistanceUnit.KM, UserStatus.ACTIVE, Instant.now(), Instant.now());

      passwordIdentity = AuthIdentity.restore(
          UUID.randomUUID(), emailUser.getId(), AuthProvider.PASSWORD,
          null, "maria@example.com", "+5511999999999", "hashed_password",
          true, null, Instant.now(), Instant.now());

      lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
      lenient().when(verificationTokenRepository.save(any(VerificationToken.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("should generate token and send email notification")
    void shouldGenerateTokenAndSendEmailNotification() {
      RequestPasswordResetInputDto input = new RequestPasswordResetInputDto("maria@example.com", null);

      when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(emailUser));
      when(authIdentityRepository.findByUserIdAndProvider(emailUser.getId(), AuthProvider.PASSWORD))
          .thenReturn(Optional.of(passwordIdentity));

      requestPasswordResetService.execute(input);

      ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
      verify(verificationTokenRepository).save(tokenCaptor.capture());

      VerificationToken savedToken = tokenCaptor.getValue();
      assertEquals(TokenType.PASSWORD_RESET, savedToken.getType());
      assertEquals(emailUser.getId(), savedToken.getUserId());
      assertNotNull(savedToken.getToken());
      assertNotNull(savedToken.getExpiresAt());

      verify(notificationSender).sendEmail(
          "maria@example.com",
          "Password reset instructions",
          "Use this token to reset your password: " + savedToken.getToken());
      verify(notificationSender, never()).sendSms(anyString(), anyString());
      verify(meterRegistry).counter("users.password_reset.request", "channel", "email", "status", "sent");
      verify(counter).increment();
    }

    @Test
    @DisplayName("should generate token and send sms notification")
    void shouldGenerateTokenAndSendSmsNotification() {
      RequestPasswordResetInputDto input = new RequestPasswordResetInputDto(null, "+5511999999999");

      when(userRepository.findByPhoneNumber("+5511999999999")).thenReturn(Optional.of(phoneUser));
      when(authIdentityRepository.findByUserIdAndProvider(phoneUser.getId(), AuthProvider.PASSWORD))
          .thenReturn(Optional.of(AuthIdentity.restore(
              passwordIdentity.getId(), phoneUser.getId(), AuthProvider.PASSWORD,
              null, "maria@example.com", "+5511999999999", "hashed_password",
              true, null, Instant.now(), Instant.now())));

      requestPasswordResetService.execute(input);

      ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
      verify(verificationTokenRepository).save(tokenCaptor.capture());

      VerificationToken savedToken = tokenCaptor.getValue();
      verify(notificationSender).sendSms(
          "+5511999999999",
          "Password reset token: " + savedToken.getToken());
      verify(notificationSender, never()).sendEmail(anyString(), anyString(), anyString());
      verify(meterRegistry).counter("users.password_reset.request", "channel", "sms", "status", "sent");
      verify(counter).increment();
    }

    @Test
    @DisplayName("should ignore request when user is not found")
    void shouldIgnoreRequestWhenUserIsNotFound() {
      RequestPasswordResetInputDto input = new RequestPasswordResetInputDto("missing@example.com", null);

      when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

      requestPasswordResetService.execute(input);

      verify(authIdentityRepository, never()).findByUserIdAndProvider(any(), any());
      verify(verificationTokenRepository, never()).save(any());
      verify(notificationSender, never()).sendEmail(anyString(), anyString(), anyString());
      verify(notificationSender, never()).sendSms(anyString(), anyString());
    }

    @Test
    @DisplayName("should ignore request when password identity is not found")
    void shouldIgnoreRequestWhenPasswordIdentityIsNotFound() {
      RequestPasswordResetInputDto input = new RequestPasswordResetInputDto("maria@example.com", null);

      when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(emailUser));
      when(authIdentityRepository.findByUserIdAndProvider(emailUser.getId(), AuthProvider.PASSWORD))
          .thenReturn(Optional.empty());

      requestPasswordResetService.execute(input);

      verify(verificationTokenRepository, never()).save(any());
      verify(notificationSender, never()).sendEmail(anyString(), anyString(), anyString());
      verify(notificationSender, never()).sendSms(anyString(), anyString());
    }
  }
}