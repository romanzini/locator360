package com.locator360.api.rest.user;

import com.locator360.api.rest.config.GlobalExceptionHandler;
import com.locator360.api.rest.config.JwtAuthenticationFilter;
import com.locator360.api.rest.config.SecurityConfig;
import com.locator360.core.port.in.dto.input.UpdateUserProfileInputDto;
import com.locator360.core.port.in.dto.output.UserProfileOutputDto;
import com.locator360.core.port.in.user.GetUserProfileUseCase;
import com.locator360.core.port.in.user.UpdateUserProfileUseCase;
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
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class })
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private GetUserProfileUseCase getUserProfileUseCase;

  @MockitoBean
  private UpdateUserProfileUseCase updateUserProfileUseCase;

  @MockitoBean
  private TokenProvider tokenProvider;

  // ─── GET /api/v1/users/me ─────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/users/me")
  class GetProfileTests {

    @Test
    @DisplayName("should return 200 with user profile")
    void shouldReturn200WithProfile() throws Exception {
      UUID userId = UUID.randomUUID();
      when(tokenProvider.validateToken("valid-jwt-token")).thenReturn(userId);

      UserProfileOutputDto output = UserProfileOutputDto.builder()
          .id(userId)
          .email("maria@example.com")
          .phoneNumber("+5511999999999")
          .fullName("Maria Oliveira")
          .firstName("Maria")
          .lastName("Oliveira")
          .preferredLanguage("pt-BR")
          .timezone("America/Sao_Paulo")
          .distanceUnit("KM")
          .status("ACTIVE")
          .createdAt(Instant.parse("2026-03-01T10:00:00Z"))
          .updatedAt(Instant.parse("2026-03-01T10:00:00Z"))
          .build();

      when(getUserProfileUseCase.execute(userId)).thenReturn(output);

      mockMvc.perform(get("/api/v1/users/me")
              .header("Authorization", "Bearer valid-jwt-token"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(userId.toString()))
          .andExpect(jsonPath("$.email").value("maria@example.com"))
          .andExpect(jsonPath("$.fullName").value("Maria Oliveira"))
          .andExpect(jsonPath("$.preferredLanguage").value("pt-BR"))
          .andExpect(jsonPath("$.distanceUnit").value("KM"))
          .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/users/me"))
          .andExpect(status().isUnauthorized());

      verify(getUserProfileUseCase, never()).execute(any());
    }
  }

  // ─── PATCH /api/v1/users/me ───────────────────────────────────

  @Nested
  @DisplayName("PATCH /api/v1/users/me")
  class UpdateProfileTests {

    @Test
    @DisplayName("should return 200 with updated profile")
    void shouldReturn200WithUpdatedProfile() throws Exception {
      UUID userId = UUID.randomUUID();
      when(tokenProvider.validateToken("valid-jwt-token")).thenReturn(userId);

      UserProfileOutputDto output = UserProfileOutputDto.builder()
          .id(userId)
          .email("maria@example.com")
          .fullName("Ana Paula Santos")
          .firstName("Ana Paula")
          .lastName("Santos")
          .birthDate(LocalDate.of(1995, 3, 20))
          .gender("F")
          .preferredLanguage("en-US")
          .timezone("America/New_York")
          .distanceUnit("MILES")
          .status("ACTIVE")
          .createdAt(Instant.parse("2026-03-01T10:00:00Z"))
          .updatedAt(Instant.parse("2026-03-10T10:00:00Z"))
          .build();

      when(updateUserProfileUseCase.execute(eq(userId), any(UpdateUserProfileInputDto.class)))
          .thenReturn(output);

      String requestBody = """
          {
            "fullName": "Ana Paula Santos",
            "birthDate": "1995-03-20",
            "gender": "F",
            "preferredLanguage": "en-US",
            "timezone": "America/New_York",
            "distanceUnit": "MILES"
          }
          """;

      mockMvc.perform(patch("/api/v1/users/me")
              .header("Authorization", "Bearer valid-jwt-token")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.fullName").value("Ana Paula Santos"))
          .andExpect(jsonPath("$.gender").value("F"))
          .andExpect(jsonPath("$.distanceUnit").value("MILES"));
    }

    @Test
    @DisplayName("should return 200 when updating only one field")
    void shouldReturn200WhenUpdatingPartialFields() throws Exception {
      UUID userId = UUID.randomUUID();
      when(tokenProvider.validateToken("valid-jwt-token")).thenReturn(userId);

      UserProfileOutputDto output = UserProfileOutputDto.builder()
          .id(userId)
          .fullName("Maria Oliveira")
          .preferredLanguage("en-US")
          .distanceUnit("KM")
          .status("ACTIVE")
          .build();

      when(updateUserProfileUseCase.execute(eq(userId), any(UpdateUserProfileInputDto.class)))
          .thenReturn(output);

      String requestBody = """
          {
            "preferredLanguage": "en-US"
          }
          """;

      mockMvc.perform(patch("/api/v1/users/me")
              .header("Authorization", "Bearer valid-jwt-token")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.preferredLanguage").value("en-US"));
    }

    @Test
    @DisplayName("should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(patch("/api/v1/users/me")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
          .andExpect(status().isUnauthorized());

      verify(updateUserProfileUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("should return 422 when user not found")
    void shouldReturn422WhenUserNotFound() throws Exception {
      UUID userId = UUID.randomUUID();
      when(tokenProvider.validateToken("valid-jwt-token")).thenReturn(userId);
      when(updateUserProfileUseCase.execute(eq(userId), any(UpdateUserProfileInputDto.class)))
          .thenThrow(new IllegalArgumentException("User not found"));

      mockMvc.perform(patch("/api/v1/users/me")
              .header("Authorization", "Bearer valid-jwt-token")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
          .andExpect(status().isUnprocessableEntity());
    }
  }
}
