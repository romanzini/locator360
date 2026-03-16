package com.locator360.api.rest.place;

import com.locator360.api.rest.config.GlobalExceptionHandler;
import com.locator360.api.rest.config.JwtAuthenticationFilter;
import com.locator360.api.rest.config.SecurityConfig;
import com.locator360.core.port.in.dto.input.CreatePlaceInputDto;
import com.locator360.core.port.in.dto.input.UpdatePlaceInputDto;
import com.locator360.core.port.in.dto.output.PlaceOutputDto;
import com.locator360.core.port.in.place.CreatePlaceUseCase;
import com.locator360.core.port.in.place.DeletePlaceUseCase;
import com.locator360.core.port.in.place.GetPlaceUseCase;
import com.locator360.core.port.in.place.ListPlacesUseCase;
import com.locator360.core.port.in.place.UpdatePlaceUseCase;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlaceController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class })
class PlaceControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CreatePlaceUseCase createPlaceUseCase;

  @MockitoBean
  private UpdatePlaceUseCase updatePlaceUseCase;

  @MockitoBean
  private DeletePlaceUseCase deletePlaceUseCase;

  @MockitoBean
  private ListPlacesUseCase listPlacesUseCase;

  @MockitoBean
  private GetPlaceUseCase getPlaceUseCase;

  @MockitoBean
  private TokenProvider tokenProvider;

  // ─── GET /api/v1/circles/{circleId}/places ──────────────────────

  @Nested
  @DisplayName("GET /api/v1/circles/{circleId}/places")
  class ListPlacesTests {

    private final UUID userId = UUID.randomUUID();
    private final UUID circleId = UUID.randomUUID();
    private final String validToken = "valid-jwt-token";

    @Test
    @DisplayName("should return 200 with list of places")
    void shouldReturn200WithPlaces() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);

      PlaceOutputDto place1 = PlaceOutputDto.builder()
          .id(UUID.randomUUID())
          .circleId(circleId)
          .name("Casa")
          .type("HOME")
          .latitude(-23.5)
          .longitude(-46.6)
          .radiusMeters(100.0)
          .active(true)
          .createdByUserId(userId)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .build();

      when(listPlacesUseCase.execute(userId, circleId))
          .thenReturn(List.of(place1));

      mockMvc.perform(get("/api/v1/circles/{circleId}/places", circleId)
          .header("Authorization", "Bearer " + validToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].name").value("Casa"))
          .andExpect(jsonPath("$[0].type").value("HOME"));

      verify(listPlacesUseCase).execute(userId, circleId);
    }

    @Test
    @DisplayName("should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/v1/circles/{circleId}/places", circleId))
          .andExpect(status().isUnauthorized());

      verify(listPlacesUseCase, never()).execute(any(), any());
    }
  }

  // ─── POST /api/v1/circles/{circleId}/places ─────────────────────

  @Nested
  @DisplayName("POST /api/v1/circles/{circleId}/places")
  class CreatePlaceTests {

    private final UUID userId = UUID.randomUUID();
    private final UUID circleId = UUID.randomUUID();
    private final String validToken = "valid-jwt-token";

    @Test
    @DisplayName("should return 201 when place is created successfully")
    void shouldReturn201WhenSuccessful() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);

      PlaceOutputDto output = PlaceOutputDto.builder()
          .id(UUID.randomUUID())
          .circleId(circleId)
          .name("Escola do Pedro")
          .type("SCHOOL")
          .addressText("Av. das Escolas, 500")
          .latitude(-23.5605)
          .longitude(-46.65)
          .radiusMeters(150.0)
          .active(true)
          .createdByUserId(userId)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .build();

      when(createPlaceUseCase.execute(eq(userId), eq(circleId), any(CreatePlaceInputDto.class)))
          .thenReturn(output);

      String requestBody = """
          {
              "name": "Escola do Pedro",
              "type": "SCHOOL",
              "addressText": "Av. das Escolas, 500",
              "latitude": -23.5605,
              "longitude": -46.6500,
              "radiusMeters": 150.0
          }
          """;

      mockMvc.perform(post("/api/v1/circles/{circleId}/places", circleId)
          .header("Authorization", "Bearer " + validToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value("Escola do Pedro"))
          .andExpect(jsonPath("$.type").value("SCHOOL"))
          .andExpect(jsonPath("$.radiusMeters").value(150.0));

      verify(createPlaceUseCase).execute(eq(userId), eq(circleId), any(CreatePlaceInputDto.class));
    }

    @Test
    @DisplayName("should return 400 when name is missing")
    void shouldReturn400WhenNameMissing() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);

      String requestBody = """
          {
              "latitude": -23.5,
              "longitude": -46.6,
              "radiusMeters": 100.0
          }
          """;

      mockMvc.perform(post("/api/v1/circles/{circleId}/places", circleId)
          .header("Authorization", "Bearer " + validToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isBadRequest());

      verify(createPlaceUseCase, never()).execute(any(), any(), any());
    }

    @Test
    @DisplayName("should return 400 when latitude is missing")
    void shouldReturn400WhenLatitudeMissing() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);

      String requestBody = """
          {
              "name": "Casa",
              "longitude": -46.6,
              "radiusMeters": 100.0
          }
          """;

      mockMvc.perform(post("/api/v1/circles/{circleId}/places", circleId)
          .header("Authorization", "Bearer " + validToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isBadRequest());

      verify(createPlaceUseCase, never()).execute(any(), any(), any());
    }

    @Test
    @DisplayName("should return 400 when radiusMeters is negative")
    void shouldReturn400WhenRadiusNegative() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);

      String requestBody = """
          {
              "name": "Casa",
              "latitude": -23.5,
              "longitude": -46.6,
              "radiusMeters": -10.0
          }
          """;

      mockMvc.perform(post("/api/v1/circles/{circleId}/places", circleId)
          .header("Authorization", "Bearer " + validToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isBadRequest());

      verify(createPlaceUseCase, never()).execute(any(), any(), any());
    }

    @Test
    @DisplayName("should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      String requestBody = """
          {
              "name": "Casa",
              "latitude": -23.5,
              "longitude": -46.6,
              "radiusMeters": 100.0
          }
          """;

      mockMvc.perform(post("/api/v1/circles/{circleId}/places", circleId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isUnauthorized());

      verify(createPlaceUseCase, never()).execute(any(), any(), any());
    }

    @Test
    @DisplayName("should return 422 when user is not a member")
    void shouldReturn422WhenUserNotMember() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);
      when(createPlaceUseCase.execute(eq(userId), eq(circleId), any(CreatePlaceInputDto.class)))
          .thenThrow(new IllegalArgumentException("User is not a member of this circle"));

      String requestBody = """
          {
              "name": "Casa",
              "latitude": -23.5,
              "longitude": -46.6,
              "radiusMeters": 100.0
          }
          """;

      mockMvc.perform(post("/api/v1/circles/{circleId}/places", circleId)
          .header("Authorization", "Bearer " + validToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isUnprocessableEntity());
    }
  }

  // ─── GET /api/v1/circles/{circleId}/places/{placeId} ────────────

  @Nested
  @DisplayName("GET /api/v1/circles/{circleId}/places/{placeId}")
  class GetPlaceTests {

    private final UUID userId = UUID.randomUUID();
    private final UUID circleId = UUID.randomUUID();
    private final UUID placeId = UUID.randomUUID();
    private final String validToken = "valid-jwt-token";

    @Test
    @DisplayName("should return 200 with place details")
    void shouldReturn200WithPlaceDetails() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);

      PlaceOutputDto output = PlaceOutputDto.builder()
          .id(placeId)
          .circleId(circleId)
          .name("Casa")
          .type("HOME")
          .latitude(-23.5)
          .longitude(-46.6)
          .radiusMeters(100.0)
          .active(true)
          .createdByUserId(userId)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .build();

      when(getPlaceUseCase.execute(userId, circleId, placeId)).thenReturn(output);

      mockMvc.perform(get("/api/v1/circles/{circleId}/places/{placeId}", circleId, placeId)
          .header("Authorization", "Bearer " + validToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("Casa"))
          .andExpect(jsonPath("$.id").value(placeId.toString()));

      verify(getPlaceUseCase).execute(userId, circleId, placeId);
    }

    @Test
    @DisplayName("should return 422 when place not found")
    void shouldReturn422WhenPlaceNotFound() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);
      when(getPlaceUseCase.execute(userId, circleId, placeId))
          .thenThrow(new IllegalArgumentException("Place not found"));

      mockMvc.perform(get("/api/v1/circles/{circleId}/places/{placeId}", circleId, placeId)
          .header("Authorization", "Bearer " + validToken))
          .andExpect(status().isUnprocessableEntity());
    }
  }

  // ─── PATCH /api/v1/circles/{circleId}/places/{placeId} ──────────

  @Nested
  @DisplayName("PATCH /api/v1/circles/{circleId}/places/{placeId}")
  class UpdatePlaceTests {

    private final UUID userId = UUID.randomUUID();
    private final UUID circleId = UUID.randomUUID();
    private final UUID placeId = UUID.randomUUID();
    private final String validToken = "valid-jwt-token";

    @Test
    @DisplayName("should return 200 when place is updated successfully")
    void shouldReturn200WhenSuccessful() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);

      PlaceOutputDto output = PlaceOutputDto.builder()
          .id(placeId)
          .circleId(circleId)
          .name("Escola")
          .type("SCHOOL")
          .latitude(-22.0)
          .longitude(-45.0)
          .radiusMeters(200.0)
          .active(true)
          .createdByUserId(userId)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .build();

      when(updatePlaceUseCase.execute(eq(userId), eq(circleId), eq(placeId),
          any(UpdatePlaceInputDto.class)))
          .thenReturn(output);

      String requestBody = """
          {
              "name": "Escola",
              "type": "SCHOOL",
              "radiusMeters": 200.0
          }
          """;

      mockMvc.perform(patch("/api/v1/circles/{circleId}/places/{placeId}", circleId, placeId)
          .header("Authorization", "Bearer " + validToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("Escola"))
          .andExpect(jsonPath("$.type").value("SCHOOL"));

      verify(updatePlaceUseCase).execute(eq(userId), eq(circleId), eq(placeId),
          any(UpdatePlaceInputDto.class));
    }

    @Test
    @DisplayName("should return 422 when place not found")
    void shouldReturn422WhenPlaceNotFound() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);
      when(updatePlaceUseCase.execute(eq(userId), eq(circleId), eq(placeId),
          any(UpdatePlaceInputDto.class)))
          .thenThrow(new IllegalArgumentException("Place not found"));

      String requestBody = """
          {
              "name": "Escola"
          }
          """;

      mockMvc.perform(patch("/api/v1/circles/{circleId}/places/{placeId}", circleId, placeId)
          .header("Authorization", "Bearer " + validToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isUnprocessableEntity());
    }
  }

  // ─── DELETE /api/v1/circles/{circleId}/places/{placeId} ─────────

  @Nested
  @DisplayName("DELETE /api/v1/circles/{circleId}/places/{placeId}")
  class DeletePlaceTests {

    private final UUID userId = UUID.randomUUID();
    private final UUID circleId = UUID.randomUUID();
    private final UUID placeId = UUID.randomUUID();
    private final String validToken = "valid-jwt-token";

    @Test
    @DisplayName("should return 204 when place is deleted successfully")
    void shouldReturn204WhenSuccessful() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);

      doNothing().when(deletePlaceUseCase).execute(userId, circleId, placeId);

      mockMvc.perform(delete("/api/v1/circles/{circleId}/places/{placeId}", circleId, placeId)
          .header("Authorization", "Bearer " + validToken))
          .andExpect(status().isNoContent());

      verify(deletePlaceUseCase).execute(userId, circleId, placeId);
    }

    @Test
    @DisplayName("should return 422 when place not found")
    void shouldReturn422WhenPlaceNotFound() throws Exception {
      when(tokenProvider.validateToken(validToken)).thenReturn(userId);
      doThrow(new IllegalArgumentException("Place not found"))
          .when(deletePlaceUseCase).execute(userId, circleId, placeId);

      mockMvc.perform(delete("/api/v1/circles/{circleId}/places/{placeId}", circleId, placeId)
          .header("Authorization", "Bearer " + validToken))
          .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc.perform(delete("/api/v1/circles/{circleId}/places/{placeId}", circleId, placeId))
          .andExpect(status().isUnauthorized());

      verify(deletePlaceUseCase, never()).execute(any(), any(), any());
    }
  }
}
