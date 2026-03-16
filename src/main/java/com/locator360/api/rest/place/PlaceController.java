package com.locator360.api.rest.place;

import com.locator360.core.port.in.dto.input.CreatePlaceInputDto;
import com.locator360.core.port.in.dto.input.UpdatePlaceInputDto;
import com.locator360.core.port.in.dto.output.PlaceOutputDto;
import com.locator360.core.port.in.place.CreatePlaceUseCase;
import com.locator360.core.port.in.place.DeletePlaceUseCase;
import com.locator360.core.port.in.place.GetPlaceUseCase;
import com.locator360.core.port.in.place.ListPlacesUseCase;
import com.locator360.core.port.in.place.UpdatePlaceUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/circles/{circleId}/places")
@RequiredArgsConstructor
@Slf4j
public class PlaceController implements PlaceControllerApi {

  private final CreatePlaceUseCase createPlaceUseCase;
  private final UpdatePlaceUseCase updatePlaceUseCase;
  private final DeletePlaceUseCase deletePlaceUseCase;
  private final ListPlacesUseCase listPlacesUseCase;
  private final GetPlaceUseCase getPlaceUseCase;

  @Override
  public ResponseEntity<List<PlaceOutputDto>> list(@PathVariable UUID circleId) {
    UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.debug("Received list places request for circle: {} from user: {}", circleId, userId);

    List<PlaceOutputDto> places = listPlacesUseCase.execute(userId, circleId);
    log.debug("Returning {} places for circle: {}", places.size(), circleId);

    return ResponseEntity.ok(places);
  }

  @Override
  public ResponseEntity<PlaceOutputDto> create(@PathVariable UUID circleId,
      @Valid @RequestBody CreatePlaceInputDto input) {
    UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.debug("Received create place request for circle: {} from user: {}", circleId, userId);

    PlaceOutputDto output = createPlaceUseCase.execute(userId, circleId, input);
    log.info("Place created: {} in circle: {}", output.getId(), circleId);

    return ResponseEntity.status(HttpStatus.CREATED).body(output);
  }

  @Override
  public ResponseEntity<PlaceOutputDto> get(@PathVariable UUID circleId,
      @PathVariable UUID placeId) {
    UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.debug("Received get place request: {} in circle: {} from user: {}", placeId, circleId, userId);

    PlaceOutputDto output = getPlaceUseCase.execute(userId, circleId, placeId);

    return ResponseEntity.ok(output);
  }

  @Override
  public ResponseEntity<PlaceOutputDto> update(@PathVariable UUID circleId,
      @PathVariable UUID placeId,
      @Valid @RequestBody UpdatePlaceInputDto input) {
    UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.debug("Received update place request: {} in circle: {} from user: {}", placeId, circleId, userId);

    PlaceOutputDto output = updatePlaceUseCase.execute(userId, circleId, placeId, input);
    log.info("Place updated: {} in circle: {}", placeId, circleId);

    return ResponseEntity.ok(output);
  }

  @Override
  public ResponseEntity<Void> delete(@PathVariable UUID circleId,
      @PathVariable UUID placeId) {
    UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.debug("Received delete place request: {} in circle: {} from user: {}", placeId, circleId, userId);

    deletePlaceUseCase.execute(userId, circleId, placeId);
    log.info("Place deleted: {} in circle: {}", placeId, circleId);

    return ResponseEntity.noContent().build();
  }
}
