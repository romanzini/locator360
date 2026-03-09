package com.locator360.api.rest.auth;

import com.locator360.api.rest.config.GlobalExceptionHandler;
import com.locator360.api.rest.config.SecurityConfig;
import com.locator360.core.port.in.auth.RegisterUserUseCase;
import com.locator360.core.port.in.dto.input.RegisterWithEmailInputDto;
import com.locator360.core.port.in.dto.input.RegisterWithPhoneInputDto;
import com.locator360.core.port.in.dto.output.RegisterUserOutputDto;
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
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private RegisterUserUseCase registerUserUseCase;

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
}
