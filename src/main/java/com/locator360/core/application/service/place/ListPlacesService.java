package com.locator360.core.application.service.place;

import com.locator360.core.domain.place.Place;
import com.locator360.core.port.in.dto.output.PlaceOutputDto;
import com.locator360.core.port.in.place.ListPlacesUseCase;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListPlacesService implements ListPlacesUseCase {

  private final PlaceRepository placeRepository;
  private final CircleMemberRepository circleMemberRepository;
  private final ModelMapper modelMapper;

  @Override
  public List<PlaceOutputDto> execute(UUID userId, UUID circleId) {
    log.debug("Listing places for circle: {} by user: {}", circleId, userId);

    circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
        .orElseThrow(() -> new IllegalArgumentException("User is not a member of this circle"));

    List<Place> places = placeRepository.findActiveByCircleId(circleId);
    log.debug("Found {} active places for circle: {}", places.size(), circleId);

    return places.stream()
      .map(place -> PlaceOutputDto.builder()
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
        .build())
      .collect(Collectors.toList());
  }
}
