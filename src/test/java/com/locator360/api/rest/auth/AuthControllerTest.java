package com.locator360.api.rest.auth;

import com.locator360.api.rest.config.GlobalExceptionHandler;
import com.locator360.api.rest.config.JwtAuthenticationFilter;
import com.locator360.api.rest.config.SecurityConfig;
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
import com.locator360.core.port.out.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class })
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private RegisterUserUseCase registerUserUseCase;

        @MockitoBean
        private LoginUseCase loginUseCase;

        @MockitoBean
        private RefreshTokenUseCase refreshTokenUseCase;

        @MockitoBean
        private LogoutUseCase logoutUseCase;

        @MockitoBean
        private RequestPasswordResetUseCase requestPasswordResetUseCase;

        @MockitoBean
        private ConfirmPasswordResetUseCase confirmPasswordResetUseCase;

        @MockitoBean
        private TokenProvider tokenProvider;

        // ─── POST /api/v1/auth/register (email) ─────────────────────────

        @Nested
        @DisplayName("POST /api/v1/auth/register/email")
        class RegisterWithEmailTests {

                @Test
                @DisplayName("should return 201 Created when registration is successful")
                void shouldReturn201WhenSuccessful() throws Exception {
                        RegisterUserOutputDto output = RegisterUserOutputDto.builder()
                                        .id(UUID.randomUUID())
                                        .email("maria@example.com")
                                        .fullName("Maria Oliveira")
                                        .firstName("Maria")
                                        .lastName("Oliveira")
                                        .preferredLanguage("pt-BR")
                                        .timezone("America/Sao_Paulo")
                                        .distanceUnit("KM")
                                        .status("PENDING_VERIFICATION")
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        when(registerUserUseCase.registerWithEmail(any(RegisterWithEmailInputDto.class)))
                                        .thenReturn(output);

                        String requestBody = """
                                        {
                                            "email": "maria@example.com",
                                            "password": "SenhaForte123!",
                                            "fullName": "Maria Oliveira"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/register/email")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.email").value("maria@example.com"))
                                        .andExpect(jsonPath("$.fullName").value("Maria Oliveira"))
                                        .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"));

                        verify(registerUserUseCase).registerWithEmail(any(RegisterWithEmailInputDto.class));
                }

                @Test
                @DisplayName("should return 400 when email is missing")
                void shouldReturn400WhenEmailMissing() throws Exception {
                        String requestBody = """
                                        {
                                            "password": "SenhaForte123!",
                                            "fullName": "Maria Oliveira"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/register/email")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(registerUserUseCase, never()).registerWithEmail(any());
                }

                @Test
                @DisplayName("should return 400 when password is too short")
                void shouldReturn400WhenPasswordTooShort() throws Exception {
                        String requestBody = """
                                        {
                                            "email": "maria@example.com",
                                            "password": "12345",
                                            "fullName": "Maria Oliveira"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/register/email")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(registerUserUseCase, never()).registerWithEmail(any());
                }

                @Test
                @DisplayName("should return 400 when fullName is missing")
                void shouldReturn400WhenFullNameMissing() throws Exception {
                        String requestBody = """
                                        {
                                            "email": "maria@example.com",
                                            "password": "SenhaForte123!"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/register/email")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(registerUserUseCase, never()).registerWithEmail(any());
                }

                @Test
                @DisplayName("should return 422 when email already registered")
                void shouldReturn422WhenEmailAlreadyRegistered() throws Exception {
                        when(registerUserUseCase.registerWithEmail(any(RegisterWithEmailInputDto.class)))
                                        .thenThrow(new IllegalArgumentException("Email already registered"));

                        String requestBody = """
                                        {
                                            "email": "maria@example.com",
                                            "password": "SenhaForte123!",
                                            "fullName": "Maria Oliveira"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/register/email")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isUnprocessableEntity());
                }
        }

        // ─── POST /api/v1/auth/register (phone) ─────────────────────────

        @Nested
        @DisplayName("POST /api/v1/auth/register/phone")
        class RegisterWithPhoneTests {

                @Test
                @DisplayName("should return 201 Created when phone registration is successful")
                void shouldReturn201WhenSuccessful() throws Exception {
                        RegisterUserOutputDto output = RegisterUserOutputDto.builder()
                                        .id(UUID.randomUUID())
                                        .phoneNumber("+5511999999999")
                                        .fullName("João da Silva")
                                        .firstName("João")
                                        .lastName("Silva")
                                        .preferredLanguage("pt-BR")
                                        .timezone("America/Sao_Paulo")
                                        .distanceUnit("KM")
                                        .status("PENDING_VERIFICATION")
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        when(registerUserUseCase.registerWithPhone(any(RegisterWithPhoneInputDto.class)))
                                        .thenReturn(output);

                        String requestBody = """
                                        {
                                            "phoneNumber": "+5511999999999",
                                            "verificationCode": "123456",
                                            "fullName": "João da Silva"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/register/phone")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.phoneNumber").value("+5511999999999"))
                                        .andExpect(jsonPath("$.fullName").value("João da Silva"))
                                        .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"));

                        verify(registerUserUseCase).registerWithPhone(any(RegisterWithPhoneInputDto.class));
                }

                @Test
                @DisplayName("should return 400 when phoneNumber is missing")
                void shouldReturn400WhenPhoneMissing() throws Exception {
                        String requestBody = """
                                        {
                                            "verificationCode": "123456",
                                            "fullName": "João da Silva"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/register/phone")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(registerUserUseCase, never()).registerWithPhone(any());
                }

                @Test
                @DisplayName("should return 422 when phone number already registered")
                void shouldReturn422WhenPhoneAlreadyRegistered() throws Exception {
                        when(registerUserUseCase.registerWithPhone(any(RegisterWithPhoneInputDto.class)))
                                        .thenThrow(new IllegalArgumentException("Phone number already registered"));

                        String requestBody = """
                                        {
                                            "phoneNumber": "+5511999999999",
                                            "verificationCode": "123456",
                                            "fullName": "João da Silva"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/register/phone")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isUnprocessableEntity());
                }
        }

        // ─── POST /api/v1/auth/login/email ────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/auth/login/email")
        class LoginWithEmailTests {

                private LoginOutputDto buildLoginOutput() {
                        return LoginOutputDto.builder()
                                        .accessToken("access_token_123")
                                        .refreshToken("refresh_token_123")
                                        .tokenType("Bearer")
                                        .expiresIn(3600)
                                        .user(LoginOutputDto.UserInfo.builder()
                                                        .id(UUID.randomUUID())
                                                        .email("maria@example.com")
                                                        .fullName("Maria Oliveira")
                                                        .preferredLanguage("pt-BR")
                                                        .timezone("America/Sao_Paulo")
                                                        .distanceUnit("KM")
                                                        .status("ACTIVE")
                                                        .createdAt(Instant.now())
                                                        .updatedAt(Instant.now())
                                                        .build())
                                        .build();
                }

                @Test
                @DisplayName("should return 200 when login is successful")
                void shouldReturn200WhenSuccessful() throws Exception {
                        LoginOutputDto output = buildLoginOutput();
                        when(loginUseCase.loginWithEmail(any(LoginWithEmailInputDto.class)))
                                        .thenReturn(output);

                        String requestBody = """
                                        {
                                            "email": "maria@example.com",
                                            "password": "SenhaForte123!"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/login/email")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").value("access_token_123"))
                                        .andExpect(jsonPath("$.refreshToken").value("refresh_token_123"))
                                        .andExpect(jsonPath("$.tokenType").value("Bearer"))
                                        .andExpect(jsonPath("$.expiresIn").value(3600))
                                        .andExpect(jsonPath("$.user.email").value("maria@example.com"));

                        verify(loginUseCase).loginWithEmail(any(LoginWithEmailInputDto.class));
                }

                @Test
                @DisplayName("should return 400 when email is missing")
                void shouldReturn400WhenEmailMissing() throws Exception {
                        String requestBody = """
                                        {
                                            "password": "SenhaForte123!"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/login/email")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(loginUseCase, never()).loginWithEmail(any());
                }

                @Test
                @DisplayName("should return 400 when password is too short")
                void shouldReturn400WhenPasswordTooShort() throws Exception {
                        String requestBody = """
                                        {
                                            "email": "maria@example.com",
                                            "password": "12345"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/login/email")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(loginUseCase, never()).loginWithEmail(any());
                }

                @Test
                @DisplayName("should return 422 when credentials are invalid")
                void shouldReturn422WhenInvalidCredentials() throws Exception {
                        when(loginUseCase.loginWithEmail(any(LoginWithEmailInputDto.class)))
                                        .thenThrow(new IllegalArgumentException("Invalid credentials"));

                        String requestBody = """
                                        {
                                            "email": "maria@example.com",
                                            "password": "WrongPassword123!"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/login/email")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isUnprocessableEntity());
                }
        }

        // ─── POST /api/v1/auth/login/phone ───────────────────────────

        @Nested
        @DisplayName("POST /api/v1/auth/login/phone")
        class LoginWithPhoneTests {

                @Test
                @DisplayName("should return 200 when phone login is successful")
                void shouldReturn200WhenSuccessful() throws Exception {
                        LoginOutputDto output = LoginOutputDto.builder()
                                        .accessToken("access_token_456")
                                        .refreshToken("refresh_token_456")
                                        .tokenType("Bearer")
                                        .expiresIn(3600)
                                        .user(LoginOutputDto.UserInfo.builder()
                                                        .id(UUID.randomUUID())
                                                        .phoneNumber("+5511999999999")
                                                        .fullName("João da Silva")
                                                        .preferredLanguage("pt-BR")
                                                        .timezone("America/Sao_Paulo")
                                                        .distanceUnit("KM")
                                                        .status("ACTIVE")
                                                        .createdAt(Instant.now())
                                                        .updatedAt(Instant.now())
                                                        .build())
                                        .build();

                        when(loginUseCase.loginWithPhone(any(LoginWithPhoneInputDto.class)))
                                        .thenReturn(output);

                        String requestBody = """
                                        {
                                            "phoneNumber": "+5511999999999",
                                            "verificationCode": "123456"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/login/phone")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").value("access_token_456"))
                                        .andExpect(jsonPath("$.user.phoneNumber").value("+5511999999999"));

                        verify(loginUseCase).loginWithPhone(any(LoginWithPhoneInputDto.class));
                }

                @Test
                @DisplayName("should return 400 when phoneNumber is missing")
                void shouldReturn400WhenPhoneMissing() throws Exception {
                        String requestBody = """
                                        {
                                            "verificationCode": "123456"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/login/phone")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(loginUseCase, never()).loginWithPhone(any());
                }

                @Test
                @DisplayName("should return 422 when credentials are invalid")
                void shouldReturn422WhenInvalidCredentials() throws Exception {
                        when(loginUseCase.loginWithPhone(any(LoginWithPhoneInputDto.class)))
                                        .thenThrow(new IllegalArgumentException("Invalid credentials"));

                        String requestBody = """
                                        {
                                            "phoneNumber": "+5511999999999",
                                            "verificationCode": "wrong-code"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/login/phone")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isUnprocessableEntity());
                }
        }

        // ─── POST /api/v1/auth/refresh ────────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/auth/refresh")
        class RefreshTokenTests {

                @Test
                @DisplayName("should return 200 when refresh is successful")
                void shouldReturn200WhenSuccessful() throws Exception {
                        LoginOutputDto output = LoginOutputDto.builder()
                                        .accessToken("new_access_token")
                                        .refreshToken("new_refresh_token")
                                        .tokenType("Bearer")
                                        .expiresIn(3600)
                                        .user(LoginOutputDto.UserInfo.builder()
                                                        .id(UUID.randomUUID())
                                                        .email("maria@example.com")
                                                        .fullName("Maria Oliveira")
                                                        .status("ACTIVE")
                                                        .createdAt(Instant.now())
                                                        .updatedAt(Instant.now())
                                                        .build())
                                        .build();

                        when(refreshTokenUseCase.execute(any(RefreshTokenInputDto.class)))
                                        .thenReturn(output);

                        String requestBody = """
                                        {
                                            "refreshToken": "valid_refresh_token"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").value("new_access_token"))
                                        .andExpect(jsonPath("$.refreshToken").value("new_refresh_token"))
                                        .andExpect(jsonPath("$.tokenType").value("Bearer"));

                        verify(refreshTokenUseCase).execute(any(RefreshTokenInputDto.class));
                }

                @Test
                @DisplayName("should return 400 when refreshToken is missing")
                void shouldReturn400WhenRefreshTokenMissing() throws Exception {
                        String requestBody = """
                                        {}
                                        """;

                        mockMvc.perform(post("/api/v1/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(refreshTokenUseCase, never()).execute(any());
                }

                @Test
                @DisplayName("should return 422 when refresh token is invalid")
                void shouldReturn422WhenInvalidRefreshToken() throws Exception {
                        when(refreshTokenUseCase.execute(any(RefreshTokenInputDto.class)))
                                        .thenThrow(new IllegalArgumentException("Invalid or expired token"));

                        String requestBody = """
                                        {
                                            "refreshToken": "invalid_token"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isUnprocessableEntity());
                }
        }

        // ─── POST /api/v1/auth/logout ─────────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/auth/logout")
        class LogoutTests {

                @Test
                @DisplayName("should return 204 when logout is successful")
                void shouldReturn204WhenSuccessful() throws Exception {
                        UUID userId = UUID.randomUUID();
                        when(tokenProvider.validateToken("valid-jwt-token")).thenReturn(userId);

                        mockMvc.perform(post("/api/v1/auth/logout")
                                        .header("Authorization", "Bearer valid-jwt-token"))
                                        .andExpect(status().isNoContent());

                        verify(logoutUseCase).execute(userId);
                }

                @Test
                @DisplayName("should return 401 when not authenticated")
                void shouldReturn401WhenNotAuthenticated() throws Exception {
                        mockMvc.perform(post("/api/v1/auth/logout"))
                                        .andExpect(status().isUnauthorized());

                        verify(logoutUseCase, never()).execute(any());
                }
        }

        @Nested
        @DisplayName("POST /api/v1/auth/password-reset/request")
        class RequestPasswordResetTests {

                @Test
                @DisplayName("should return 202 when email reset request is valid")
                void shouldReturn202WhenEmailResetRequestIsValid() throws Exception {
                        String requestBody = """
                                        {
                                                "email": "maria@example.com"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/password-reset/request")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isAccepted());

                        verify(requestPasswordResetUseCase).execute(any(RequestPasswordResetInputDto.class));
                }

                @Test
                @DisplayName("should return 202 when phone reset request is valid")
                void shouldReturn202WhenPhoneResetRequestIsValid() throws Exception {
                        String requestBody = """
                                        {
                                                "phoneNumber": "+5511999999999"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/password-reset/request")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isAccepted());

                        verify(requestPasswordResetUseCase).execute(any(RequestPasswordResetInputDto.class));
                }

                @Test
                @DisplayName("should return 400 when neither email nor phone is provided")
                void shouldReturn400WhenNeitherEmailNorPhoneIsProvided() throws Exception {
                        String requestBody = """
                                        {}
                                        """;

                        mockMvc.perform(post("/api/v1/auth/password-reset/request")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(requestPasswordResetUseCase, never()).execute(any());
                }
        }

        @Nested
        @DisplayName("POST /api/v1/auth/password-reset/confirm")
        class ConfirmPasswordResetTests {

                @Test
                @DisplayName("should return 200 when password reset confirmation is valid")
                void shouldReturn200WhenPasswordResetConfirmationIsValid() throws Exception {
                        String requestBody = """
                                        {
                                                "token": "reset-token-123",
                                                "newPassword": "NovaSenha123!"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk());

                        verify(confirmPasswordResetUseCase).execute(any(ConfirmPasswordResetInputDto.class));
                }

                @Test
                @DisplayName("should return 400 when token is missing")
                void shouldReturn400WhenTokenIsMissing() throws Exception {
                        String requestBody = """
                                        {
                                                "newPassword": "NovaSenha123!"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(confirmPasswordResetUseCase, never()).execute(any());
                }

                @Test
                @DisplayName("should return 400 when new password is too short")
                void shouldReturn400WhenNewPasswordIsTooShort() throws Exception {
                        String requestBody = """
                                        {
                                                "token": "reset-token-123",
                                                "newPassword": "123"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        verify(confirmPasswordResetUseCase, never()).execute(any());
                }

                @Test
                @DisplayName("should return 422 when token is invalid")
                void shouldReturn422WhenTokenIsInvalid() throws Exception {
                        doThrow(new IllegalArgumentException("Invalid or expired password reset token"))
                                        .when(confirmPasswordResetUseCase)
                                        .execute(any(ConfirmPasswordResetInputDto.class));

                        String requestBody = """
                                        {
                                                "token": "invalid-token",
                                                "newPassword": "NovaSenha123!"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isUnprocessableEntity());
                }
        }
}
