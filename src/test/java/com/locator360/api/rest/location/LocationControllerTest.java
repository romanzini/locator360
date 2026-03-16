package com.locator360.api.rest.location;

import com.locator360.api.rest.config.GlobalExceptionHandler;
import com.locator360.api.rest.config.JwtAuthenticationFilter;
import com.locator360.api.rest.config.SecurityConfig;
import com.locator360.core.port.in.dto.output.MemberLocationOutputDto;
import com.locator360.core.port.in.dto.output.SharingStatus;
import com.locator360.core.port.in.location.GetCircleMembersLocationUseCase;
import com.locator360.core.port.in.location.PauseLocationSharingUseCase;
import com.locator360.core.port.in.location.ResumeLocationSharingUseCase;
import com.locator360.core.port.in.location.StreamLocationUseCase;
import com.locator360.core.port.out.TokenProvider;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class })
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StreamLocationUseCase streamLocationUseCase;

    @MockitoBean
    private GetCircleMembersLocationUseCase getCircleMembersLocationUseCase;

    @MockitoBean
    private PauseLocationSharingUseCase pauseLocationSharingUseCase;

    @MockitoBean
    private ResumeLocationSharingUseCase resumeLocationSharingUseCase;

    @MockitoBean
    private TokenProvider tokenProvider;

    @Nested
    @DisplayName("POST /api/v1/locations/stream")
    class StreamLocationTests {

        private final UUID userId = UUID.randomUUID();
        private final String validToken = "valid-jwt-token";

        @Test
        @DisplayName("should return 202 Accepted when location events are streamed successfully")
        void shouldReturn202WhenSuccessful() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            String requestBody = """
                    {
                        "circleId": "c1f8a2c3-1111-4b22-9abc-000000000001",
                        "events": [
                            {
                                "latitude": -23.561414,
                                "longitude": -46.655881,
                                "source": "GPS",
                                "recordedAt": "2026-02-10T13:44:30Z"
                            }
                        ]
                    }
                    """;

            mockMvc.perform(post("/api/v1/locations/stream")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isAccepted());

            verify(streamLocationUseCase).execute(eq(userId), any());
        }

        @Test
        @DisplayName("should return 202 Accepted with multiple events")
        void shouldReturn202WithMultipleEvents() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            String requestBody = """
                    {
                        "events": [
                            {
                                "latitude": -23.561414,
                                "longitude": -46.655881,
                                "source": "GPS",
                                "recordedAt": "2026-02-10T13:44:30Z"
                            },
                            {
                                "latitude": -23.561500,
                                "longitude": -46.655900,
                                "accuracyMeters": 11.0,
                                "speedMps": 1.2,
                                "headingDegrees": 182,
                                "altitudeMeters": 761,
                                "source": "GPS",
                                "recordedAt": "2026-02-10T13:44:45Z",
                                "isMoving": true,
                                "batteryLevel": 72
                            }
                        ]
                    }
                    """;

            mockMvc.perform(post("/api/v1/locations/stream")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isAccepted());

            verify(streamLocationUseCase).execute(eq(userId), any());
        }

        @Test
        @DisplayName("should return 400 when events is empty")
        void shouldReturn400WhenEventsEmpty() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            String requestBody = """
                    {
                        "events": []
                    }
                    """;

            mockMvc.perform(post("/api/v1/locations/stream")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when events is missing")
        void shouldReturn400WhenEventsMissing() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            String requestBody = """
                    {
                        "circleId": "c1f8a2c3-1111-4b22-9abc-000000000001"
                    }
                    """;

            mockMvc.perform(post("/api/v1/locations/stream")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when event latitude is missing")
        void shouldReturn400WhenLatitudeMissing() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            String requestBody = """
                    {
                        "events": [
                            {
                                "longitude": -46.655881,
                                "source": "GPS",
                                "recordedAt": "2026-02-10T13:44:30Z"
                            }
                        ]
                    }
                    """;

            mockMvc.perform(post("/api/v1/locations/stream")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            String requestBody = """
                    {
                        "events": [
                            {
                                "latitude": -23.561414,
                                "longitude": -46.655881,
                                "source": "GPS",
                                "recordedAt": "2026-02-10T13:44:30Z"
                            }
                        ]
                    }
                    """;

            mockMvc.perform(post("/api/v1/locations/stream")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/circles/{circleId}/members/locations")
    class GetCircleMembersLocationTests {

        private final UUID userId = UUID.randomUUID();
        private final UUID circleId = UUID.randomUUID();
        private final String validToken = "valid-jwt-token";

        @Test
        @DisplayName("should return 200 with member locations")
        void shouldReturn200WithMemberLocations() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            UUID memberUserId = UUID.randomUUID();
            Instant lastUpdated = Instant.parse("2026-02-10T13:44:30Z");

            List<MemberLocationOutputDto> locations = List.of(
                    MemberLocationOutputDto.builder()
                            .userId(memberUserId)
                            .fullName("John Doe")
                            .profilePhotoUrl("https://photo.url/john.jpg")
                            .latitude(-23.561414)
                            .longitude(-46.655881)
                            .accuracy(10.0)
                            .speed(1.5)
                            .isMoving(true)
                            .batteryLevel(85)
                            .lastUpdatedAt(lastUpdated)
                            .sharingStatus(SharingStatus.ONLINE)
                            .build());

            when(getCircleMembersLocationUseCase.execute(userId, circleId))
                    .thenReturn(locations);

            mockMvc.perform(get("/api/v1/circles/{circleId}/members/locations", circleId)
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].userId").value(memberUserId.toString()))
                    .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                    .andExpect(jsonPath("$[0].profilePhotoUrl").value("https://photo.url/john.jpg"))
                    .andExpect(jsonPath("$[0].latitude").value(-23.561414))
                    .andExpect(jsonPath("$[0].longitude").value(-46.655881))
                    .andExpect(jsonPath("$[0].accuracy").value(10.0))
                    .andExpect(jsonPath("$[0].speed").value(1.5))
                    .andExpect(jsonPath("$[0].isMoving").value(true))
                    .andExpect(jsonPath("$[0].batteryLevel").value(85))
                    .andExpect(jsonPath("$[0].sharingStatus").value("ONLINE"));

            verify(getCircleMembersLocationUseCase).execute(userId, circleId);
        }

        @Test
        @DisplayName("should return 200 with empty list when no members have locations")
        void shouldReturn200WithEmptyList() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);
            when(getCircleMembersLocationUseCase.execute(userId, circleId))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/circles/{circleId}/members/locations", circleId)
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("should return 422 when user is not a member of the circle")
        void shouldReturn422WhenNotMember() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);
            when(getCircleMembersLocationUseCase.execute(userId, circleId))
                    .thenThrow(new IllegalArgumentException("User is not a member of this circle"));

            mockMvc.perform(get("/api/v1/circles/{circleId}/members/locations", circleId)
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/circles/{circleId}/members/locations", circleId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/circles/{circleId}/location-sharing/pause")
    class PauseLocationSharingTests {

        private final UUID userId = UUID.randomUUID();
        private final UUID circleId = UUID.randomUUID();
        private final String validToken = "valid-jwt-token";

        @Test
        @DisplayName("should return 202 when location sharing is paused with pausedUntil")
        void shouldReturn202WhenPausedWithPausedUntil() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            String requestBody = """
                    {
                        "pausedUntil": "2026-12-31T23:59:59Z"
                    }
                    """;

            mockMvc.perform(post("/api/v1/circles/{circleId}/location-sharing/pause", circleId)
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isAccepted());

            verify(pauseLocationSharingUseCase).execute(eq(userId), eq(circleId), any());
        }

        @Test
        @DisplayName("should return 202 when location sharing is paused indefinitely")
        void shouldReturn202WhenPausedIndefinitely() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            String requestBody = "{}";

            mockMvc.perform(post("/api/v1/circles/{circleId}/location-sharing/pause", circleId)
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isAccepted());

            verify(pauseLocationSharingUseCase).execute(eq(userId), eq(circleId), any());
        }

        @Test
        @DisplayName("should return 422 when business validation fails")
        void shouldReturn422WhenBusinessValidationFails() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);
            doThrow(new IllegalArgumentException("User is not a member of this circle"))
                    .when(pauseLocationSharingUseCase)
                    .execute(eq(userId), eq(circleId), any());

            mockMvc.perform(post("/api/v1/circles/{circleId}/location-sharing/pause", circleId)
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/circles/{circleId}/location-sharing/pause", circleId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/circles/{circleId}/location-sharing/resume")
    class ResumeLocationSharingTests {

        private final UUID userId = UUID.randomUUID();
        private final UUID circleId = UUID.randomUUID();
        private final String validToken = "valid-jwt-token";

        @Test
        @DisplayName("should return 202 when location sharing is resumed")
        void shouldReturn202WhenResumed() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);

            mockMvc.perform(post("/api/v1/circles/{circleId}/location-sharing/resume", circleId)
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isAccepted());

            verify(resumeLocationSharingUseCase).execute(userId, circleId);
        }

        @Test
        @DisplayName("should return 422 when business validation fails")
        void shouldReturn422WhenBusinessValidationFails() throws Exception {
            when(tokenProvider.validateToken(validToken)).thenReturn(userId);
            doThrow(new IllegalArgumentException("User is not a member of this circle"))
                    .when(resumeLocationSharingUseCase)
                    .execute(userId, circleId);

            mockMvc.perform(post("/api/v1/circles/{circleId}/location-sharing/resume", circleId)
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/circles/{circleId}/location-sharing/resume", circleId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
