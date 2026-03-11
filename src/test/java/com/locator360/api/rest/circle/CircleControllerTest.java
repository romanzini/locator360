package com.locator360.api.rest.circle;

import com.locator360.api.rest.config.GlobalExceptionHandler;
import com.locator360.api.rest.config.JwtAuthenticationFilter;
import com.locator360.api.rest.config.SecurityConfig;
import com.locator360.core.port.in.circle.CreateCircleUseCase;
import com.locator360.core.port.in.circle.CreateInviteUseCase;
import com.locator360.core.port.in.circle.JoinCircleUseCase;
import com.locator360.core.port.in.dto.input.CreateCircleInputDto;
import com.locator360.core.port.in.dto.input.CreateInviteInputDto;
import com.locator360.core.port.in.dto.input.JoinCircleInputDto;
import com.locator360.core.port.in.dto.output.CircleMemberOutputDto;
import com.locator360.core.port.in.dto.output.CircleOutputDto;
import com.locator360.core.port.in.dto.output.InviteOutputDto;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CircleController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class CircleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateCircleUseCase createCircleUseCase;

    @MockitoBean
    private CreateInviteUseCase createInviteUseCase;

    @MockitoBean
    private JoinCircleUseCase joinCircleUseCase;

    @MockitoBean
    private TokenProvider tokenProvider;

    // ─── POST /api/v1/circles ───────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/circles")
    class CreateCircleTests {

        private final UUID userId = UUID.randomUUID();
        private final String validToken = "valid-jwt-token";

        @Test
        @DisplayName("should return 201 Created when circle is created successfully")
        void shouldReturn201WhenSuccessful() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            CircleOutputDto output = CircleOutputDto.builder()
                    .id(UUID.randomUUID())
                    .name("Família Silva")
                    .description("Grupo da família")
                    .photoUrl("http://photo.jpg")
                    .colorHex("#4CAF50")
                    .privacyLevel("INVITE_ONLY")
                    .createdByUserId(userId)
                    .role("ADMIN")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(createCircleUseCase.execute(eq(userId), any(CreateCircleInputDto.class)))
                    .thenReturn(output);

            String requestBody = """
                    {
                        "name": "Família Silva",
                        "description": "Grupo da família",
                        "photoUrl": "http://photo.jpg",
                        "colorHex": "#4CAF50",
                        "privacyLevel": "INVITE_ONLY"
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Família Silva"))
                    .andExpect(jsonPath("$.description").value("Grupo da família"))
                    .andExpect(jsonPath("$.privacyLevel").value("INVITE_ONLY"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));

            verify(createCircleUseCase).execute(eq(userId), any(CreateCircleInputDto.class));
        }

        @Test
        @DisplayName("should return 201 with only required fields")
        void shouldReturn201WithOnlyRequiredFields() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            CircleOutputDto output = CircleOutputDto.builder()
                    .id(UUID.randomUUID())
                    .name("Família")
                    .privacyLevel("OPEN_WITH_CODE")
                    .createdByUserId(userId)
                    .role("ADMIN")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(createCircleUseCase.execute(eq(userId), any(CreateCircleInputDto.class)))
                    .thenReturn(output);

            String requestBody = """
                    {
                        "name": "Família"
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Família"))
                    .andExpect(jsonPath("$.privacyLevel").value("OPEN_WITH_CODE"));

            verify(createCircleUseCase).execute(eq(userId), any(CreateCircleInputDto.class));
        }

        @Test
        @DisplayName("should return 400 when name is missing")
        void shouldReturn400WhenNameMissing() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            String requestBody = """
                    {
                        "description": "Grupo sem nome"
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());

            verify(createCircleUseCase, never()).execute(any(), any());
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameBlank() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            String requestBody = """
                    {
                        "name": "   "
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());

            verify(createCircleUseCase, never()).execute(any(), any());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            String requestBody = """
                    {
                        "name": "Família"
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized());

            verify(createCircleUseCase, never()).execute(any(), any());
        }
    }

    // ─── POST /api/v1/circles/{circleId}/invites ────────────────────

    @Nested
    @DisplayName("POST /api/v1/circles/{circleId}/invites")
    class CreateInviteTests {

        private final UUID userId = UUID.randomUUID();
        private final UUID circleId = UUID.randomUUID();
        private final String validToken = "valid-jwt-token";

        @Test
        @DisplayName("should return 201 Created when invite is created successfully")
        void shouldReturn201WhenSuccessful() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            InviteOutputDto output = InviteOutputDto.builder()
                    .id(UUID.randomUUID())
                    .circleId(circleId)
                    .invitedByUserId(userId)
                    .targetEmail("test@email.com")
                    .inviteCode("ABC12345")
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(createInviteUseCase.execute(eq(userId), eq(circleId), any(CreateInviteInputDto.class)))
                    .thenReturn(output);

            String requestBody = """
                    {
                        "targetEmail": "test@email.com"
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles/" + circleId + "/invites")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.circleId").value(circleId.toString()))
                    .andExpect(jsonPath("$.inviteCode").value("ABC12345"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.targetEmail").value("test@email.com"));

            verify(createInviteUseCase).execute(eq(userId), eq(circleId), any(CreateInviteInputDto.class));
        }

        @Test
        @DisplayName("should return 201 with phone number")
        void shouldReturn201WithPhoneNumber() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            InviteOutputDto output = InviteOutputDto.builder()
                    .id(UUID.randomUUID())
                    .circleId(circleId)
                    .invitedByUserId(userId)
                    .targetPhone("+5511999999999")
                    .inviteCode("XYZ78901")
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(createInviteUseCase.execute(eq(userId), eq(circleId), any(CreateInviteInputDto.class)))
                    .thenReturn(output);

            String requestBody = """
                    {
                        "targetPhone": "+5511999999999"
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles/" + circleId + "/invites")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.targetPhone").value("+5511999999999"));

            verify(createInviteUseCase).execute(eq(userId), eq(circleId), any(CreateInviteInputDto.class));
        }

        @Test
        @DisplayName("should return 201 with empty body (generic invite)")
        void shouldReturn201WithEmptyBody() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            InviteOutputDto output = InviteOutputDto.builder()
                    .id(UUID.randomUUID())
                    .circleId(circleId)
                    .inviteCode("GEN12345")
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(createInviteUseCase.execute(eq(userId), eq(circleId), any(CreateInviteInputDto.class)))
                    .thenReturn(output);

            String requestBody = "{}";

            mockMvc.perform(post("/api/v1/circles/" + circleId + "/invites")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.inviteCode").value("GEN12345"));

            verify(createInviteUseCase).execute(eq(userId), eq(circleId), any(CreateInviteInputDto.class));
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            String requestBody = """
                    {
                        "targetEmail": "test@email.com"
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles/" + circleId + "/invites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized());

            verify(createInviteUseCase, never()).execute(any(), any(), any());
        }
    }

    // ─── POST /api/v1/circles/join ──────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/circles/join")
    class JoinCircleTests {

        private final UUID userId = UUID.randomUUID();
        private final UUID circleId = UUID.randomUUID();
        private final String validToken = "valid-jwt-token";

        @Test
        @DisplayName("should return 200 OK when join is successful")
        void shouldReturn200WhenSuccessful() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            CircleMemberOutputDto output = CircleMemberOutputDto.builder()
                    .id(UUID.randomUUID())
                    .circleId(circleId)
                    .userId(userId)
                    .role("MEMBER")
                    .status("ACTIVE")
                    .joinedAt(Instant.now())
                    .build();

            when(joinCircleUseCase.execute(eq(userId), any(JoinCircleInputDto.class)))
                    .thenReturn(output);

            String requestBody = """
                    {
                        "inviteCode": "ABCD1234"
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles/join")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.circleId").value(circleId.toString()))
                    .andExpect(jsonPath("$.role").value("MEMBER"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(joinCircleUseCase).execute(eq(userId), any(JoinCircleInputDto.class));
        }

        @Test
        @DisplayName("should return 400 when inviteCode is missing")
        void shouldReturn400WhenInviteCodeMissing() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            String requestBody = "{}";

            mockMvc.perform(post("/api/v1/circles/join")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());

            verify(joinCircleUseCase, never()).execute(any(), any());
        }

        @Test
        @DisplayName("should return 400 when inviteCode is blank")
        void shouldReturn400WhenInviteCodeBlank() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            String requestBody = """
                    {
                        "inviteCode": "   "
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles/join")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());

            verify(joinCircleUseCase, never()).execute(any(), any());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            String requestBody = """
                    {
                        "inviteCode": "ABCD1234"
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized());

            verify(joinCircleUseCase, never()).execute(any(), any());
        }
    }
}
