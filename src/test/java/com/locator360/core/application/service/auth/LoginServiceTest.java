package com.locator360.core.application.service.auth;

import com.locator360.core.domain.service.AuthenticationService;
import com.locator360.core.domain.user.*;
import com.locator360.core.port.in.dto.input.LoginWithEmailInputDto;
import com.locator360.core.port.in.dto.input.LoginWithPhoneInputDto;
import com.locator360.core.port.in.dto.output.LoginOutputDto;
import com.locator360.core.port.out.AuthIdentityRepository;
import com.locator360.core.port.out.PasswordEncoder;
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
class LoginServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private AuthIdentityRepository authIdentityRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private TokenProvider tokenProvider;

  @Mock
  private AuthenticationService authenticationService;

  @Mock
  private MeterRegistry meterRegistry;

  @Mock
  private Counter counter;

  @InjectMocks
  private LoginService loginService;

  // ─── LoginWithEmail ─────────────────────────────────────────────

  @Nested
  @DisplayName("loginWithEmail")
  class LoginWithEmailTests {

    private LoginWithEmailInputDto validInput;
    private User activeUser;
    private AuthIdentity passwordIdentity;

    @BeforeEach
    void setUp() {
      validInput = new LoginWithEmailInputDto("maria@example.com", "SenhaForte123!");

      activeUser = User.restore(
          UUID.randomUUID(), "maria@example.com", null,
          "Maria Oliveira", "Maria", "Oliveira",
          null, null, null, "pt-BR", "America/Sao_Paulo",
          DistanceUnit.KM, UserStatus.ACTIVE, Instant.now(), Instant.now());

      passwordIdentity = AuthIdentity.restore(
          UUID.randomUUID(), activeUser.getId(), AuthProvider.PASSWORD,
          null, "maria@example.com", null, "hashed_password",
          true, null, Instant.now(), Instant.now());

      lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
    }

    @Test
    @DisplayName("should login successfully with valid email and password")
    void shouldLoginSuccessfully() {
      when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(activeUser));
      when(authIdentityRepository.findByUserIdAndProvider(activeUser.getId(), AuthProvider.PASSWORD))
          .thenReturn(Optional.of(passwordIdentity));
      doNothing().when(authenticationService).validateAccountStatus(activeUser);
      doNothing().when(authenticationService)
          .validatePasswordCredentials(passwordIdentity, "SenhaForte123!", passwordEncoder);
      when(tokenProvider.generateAccessToken(activeUser.getId())).thenReturn("access_token");
      when(tokenProvider.generateRefreshToken(activeUser.getId())).thenReturn("refresh_token");
      when(tokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);

      LoginOutputDto result = loginService.loginWithEmail(validInput);

      assertNotNull(result);
      assertEquals("access_token", result.getAccessToken());
      assertEquals("refresh_token", result.getRefreshToken());
      assertEquals("Bearer", result.getTokenType());
      assertEquals(3600, result.getExpiresIn());
      assertNotNull(result.getUser());
      assertEquals(activeUser.getId(), result.getUser().getId());
      assertEquals("maria@example.com", result.getUser().getEmail());
    }

    @Test
    @DisplayName("should record login on identity")
    void shouldRecordLogin() {
      when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(activeUser));
      when(authIdentityRepository.findByUserIdAndProvider(activeUser.getId(), AuthProvider.PASSWORD))
          .thenReturn(Optional.of(passwordIdentity));
      doNothing().when(authenticationService).validateAccountStatus(activeUser);
      doNothing().when(authenticationService)
          .validatePasswordCredentials(passwordIdentity, "SenhaForte123!", passwordEncoder);
      when(tokenProvider.generateAccessToken(activeUser.getId())).thenReturn("access_token");
      when(tokenProvider.generateRefreshToken(activeUser.getId())).thenReturn("refresh_token");
      when(tokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);

      loginService.loginWithEmail(validInput);

      verify(authIdentityRepository).save(passwordIdentity);
    }

    @Test
    @DisplayName("should throw when user not found by email")
    void shouldThrowWhenUserNotFound() {
      when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.empty());

      IllegalArgumentException ex = assertThrows(
          IllegalArgumentException.class,
          () -> loginService.loginWithEmail(validInput));

      assertEquals("Invalid credentials", ex.getMessage());
      verify(tokenProvider, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("should throw when auth identity not found")
    void shouldThrowWhenIdentityNotFound() {
      when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(activeUser));
      when(authIdentityRepository.findByUserIdAndProvider(activeUser.getId(), AuthProvider.PASSWORD))
          .thenReturn(Optional.empty());

      IllegalArgumentException ex = assertThrows(
          IllegalArgumentException.class,
          () -> loginService.loginWithEmail(validInput));

      assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    @DisplayName("should throw when account is blocked")
    void shouldThrowWhenAccountBlocked() {
      when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(activeUser));
      when(authIdentityRepository.findByUserIdAndProvider(activeUser.getId(), AuthProvider.PASSWORD))
          .thenReturn(Optional.of(passwordIdentity));
      doThrow(new IllegalStateException("Account is blocked"))
          .when(authenticationService).validateAccountStatus(activeUser);

      IllegalStateException ex = assertThrows(
          IllegalStateException.class,
          () -> loginService.loginWithEmail(validInput));

      assertEquals("Account is blocked", ex.getMessage());
    }

    @Test
    @DisplayName("should throw when password is invalid")
    void shouldThrowWhenPasswordInvalid() {
      when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(activeUser));
      when(authIdentityRepository.findByUserIdAndProvider(activeUser.getId(), AuthProvider.PASSWORD))
          .thenReturn(Optional.of(passwordIdentity));
      doNothing().when(authenticationService).validateAccountStatus(activeUser);
      doThrow(new IllegalArgumentException("Invalid credentials"))
          .when(authenticationService)
          .validatePasswordCredentials(passwordIdentity, "SenhaForte123!", passwordEncoder);

      IllegalArgumentException ex = assertThrows(
          IllegalArgumentException.class,
          () -> loginService.loginWithEmail(validInput));

      assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    @DisplayName("should increment login counter on success")
    void shouldIncrementCounter() {
      when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(activeUser));
      when(authIdentityRepository.findByUserIdAndProvider(activeUser.getId(), AuthProvider.PASSWORD))
          .thenReturn(Optional.of(passwordIdentity));
      doNothing().when(authenticationService).validateAccountStatus(activeUser);
      doNothing().when(authenticationService)
          .validatePasswordCredentials(passwordIdentity, "SenhaForte123!", passwordEncoder);
      when(tokenProvider.generateAccessToken(activeUser.getId())).thenReturn("access_token");
      when(tokenProvider.generateRefreshToken(activeUser.getId())).thenReturn("refresh_token");
      when(tokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);

      loginService.loginWithEmail(validInput);

      verify(meterRegistry).counter("users.login", "method", "email", "status", "success");
      verify(counter).increment();
    }
  }

  // ─── LoginWithPhone ─────────────────────────────────────────────

  @Nested
  @DisplayName("loginWithPhone")
  class LoginWithPhoneTests {

    private LoginWithPhoneInputDto validInput;
    private User activeUser;
    private AuthIdentity phoneIdentity;

    @BeforeEach
    void setUp() {
      validInput = new LoginWithPhoneInputDto("+5511999999999", "123456");

      activeUser = User.restore(
          UUID.randomUUID(), null, "+5511999999999",
          "João da Silva", "João", "da Silva",
          null, null, null, "pt-BR", "America/Sao_Paulo",
          DistanceUnit.KM, UserStatus.ACTIVE, Instant.now(), Instant.now());

      phoneIdentity = AuthIdentity.restore(
          UUID.randomUUID(), activeUser.getId(), AuthProvider.PHONE_SMS,
          null, null, "+5511999999999", null,
          true, null, Instant.now(), Instant.now());

      lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
    }

    @Test
    @DisplayName("should login successfully with valid phone and verification code")
    void shouldLoginSuccessfully() {
      when(userRepository.findByPhoneNumber("+5511999999999")).thenReturn(Optional.of(activeUser));
      when(authIdentityRepository.findByUserIdAndProvider(activeUser.getId(), AuthProvider.PHONE_SMS))
          .thenReturn(Optional.of(phoneIdentity));
      doNothing().when(authenticationService).validateAccountStatus(activeUser);
      doNothing().when(authenticationService).validatePhoneCredentials(phoneIdentity);
      when(tokenProvider.generateAccessToken(activeUser.getId())).thenReturn("access_token");
      when(tokenProvider.generateRefreshToken(activeUser.getId())).thenReturn("refresh_token");
      when(tokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);

      LoginOutputDto result = loginService.loginWithPhone(validInput);

      assertNotNull(result);
      assertEquals("access_token", result.getAccessToken());
      assertEquals("refresh_token", result.getRefreshToken());
      assertEquals("Bearer", result.getTokenType());
      assertEquals(activeUser.getId(), result.getUser().getId());
    }

    @Test
    @DisplayName("should throw when user not found by phone")
    void shouldThrowWhenUserNotFound() {
      when(userRepository.findByPhoneNumber("+5511999999999")).thenReturn(Optional.empty());

      IllegalArgumentException ex = assertThrows(
          IllegalArgumentException.class,
          () -> loginService.loginWithPhone(validInput));

      assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    @DisplayName("should throw when phone identity not found")
    void shouldThrowWhenIdentityNotFound() {
      when(userRepository.findByPhoneNumber("+5511999999999")).thenReturn(Optional.of(activeUser));
      when(authIdentityRepository.findByUserIdAndProvider(activeUser.getId(), AuthProvider.PHONE_SMS))
          .thenReturn(Optional.empty());

      IllegalArgumentException ex = assertThrows(
          IllegalArgumentException.class,
          () -> loginService.loginWithPhone(validInput));

      assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    @DisplayName("should increment login counter on phone success")
    void shouldIncrementCounter() {
      when(userRepository.findByPhoneNumber("+5511999999999")).thenReturn(Optional.of(activeUser));
      when(authIdentityRepository.findByUserIdAndProvider(activeUser.getId(), AuthProvider.PHONE_SMS))
          .thenReturn(Optional.of(phoneIdentity));
      doNothing().when(authenticationService).validateAccountStatus(activeUser);
      doNothing().when(authenticationService).validatePhoneCredentials(phoneIdentity);
      when(tokenProvider.generateAccessToken(activeUser.getId())).thenReturn("access_token");
      when(tokenProvider.generateRefreshToken(activeUser.getId())).thenReturn("refresh_token");
      when(tokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);

      loginService.loginWithPhone(validInput);

      verify(meterRegistry).counter("users.login", "method", "phone", "status", "success");
      verify(counter).increment();
    }
  }
}
