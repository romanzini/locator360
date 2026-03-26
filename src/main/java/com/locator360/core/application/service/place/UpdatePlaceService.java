package com.locator360.core.application.service.place;

import com.locator360.core.domain.place.Place;
import com.locator360.core.domain.place.PlaceType;
import com.locator360.core.port.in.dto.input.UpdatePlaceInputDto;
import com.locator360.core.port.in.dto.output.PlaceOutputDto;
import com.locator360.core.port.in.place.UpdatePlaceUseCase;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.PlaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UpdatePlaceService implements UpdatePlaceUseCase {

  private final PlaceRepository placeRepository;
  private final CircleMemberRepository circleMemberRepository;
  private final ModelMapper modelMapper;

  @Override
  public PlaceOutputDto execute(UUID userId, UUID circleId, UUID placeId, UpdatePlaceInputDto input) {
    log.debug("Updating place: {} in circle: {} by user: {}", placeId, circleId, userId);

    circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
        .orElseThrow(() -> new IllegalArgumentException("User is not a member of this circle"));

    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> new IllegalArgumentException("Place not found"));

    if (!place.getCircleId().equals(circleId)) {
      throw new IllegalArgumentException("Place does not belong to this circle");
    }

    PlaceType type = input.getType() != null ? PlaceType.valueOf(input.getType()) : null;

    place.update(input.getName(), type, input.getAddressText(),
        input.getLatitude(), input.getLongitude(), input.getRadiusMeters());

    Place savedPlace = placeRepository.save(place);
    log.info("Place updated: {} in circle: {}", placeId, circleId);

    return PlaceOutputDto.builder()
        .id(savedPlace.getId())
        .circleId(savedPlace.getCircleId())
        .name(savedPlace.getName())
        .type(savedPlace.getType() != null ? savedPlace.getType().name() : null)
        .addressText(savedPlace.getAddressText())
        .latitude(savedPlace.getLatitude())
        .longitude(savedPlace.getLongitude())
        .radiusMeters(savedPlace.getRadiusMeters())
        .active(savedPlace.isActive())
        .createdByUserId(savedPlace.getCreatedByUserId())
        .createdAt(savedPlace.getCreatedAt())
        .updatedAt(savedPlace.getUpdatedAt())
        .build();
  }
}
