package com.locator360.core.application.service.place;

import com.locator360.core.domain.place.Place;
import com.locator360.core.port.in.dto.output.PlaceOutputDto;
import com.locator360.core.port.in.place.GetPlaceUseCase;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetPlaceService implements GetPlaceUseCase {

  private final PlaceRepository placeRepository;
  private final CircleMemberRepository circleMemberRepository;
  private final ModelMapper modelMapper;

  @Override
  public PlaceOutputDto execute(UUID userId, UUID circleId, UUID placeId) {
    log.debug("Getting place: {} in circle: {} by user: {}", placeId, circleId, userId);

    circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
        .orElseThrow(() -> new IllegalArgumentException("User is not a member of this circle"));

    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> new IllegalArgumentException("Place not found"));

    if (!place.getCircleId().equals(circleId)) {
      throw new IllegalArgumentException("Place does not belong to this circle");
    }

    log.debug("Returning place: {} in circle: {}", placeId, circleId);
    return PlaceOutputDto.builder()
        .id(place.getId())
        .circleId(place.getCircleId())
        .name(place.getName())
        .type(place.getType() != null ? place.getType().name() : null)
        .addressText(place.getAddressText())
        .latitude(place.getLatitude())
        .longitude(place.getLongitude())
        .radiusMeters(place.getRadiusMeters())
        .active(place.isActive())
        .createdByUserId(place.getCreatedByUserId())
        .createdAt(place.getCreatedAt())
        .updatedAt(place.getUpdatedAt())
        .build();
  }
}
