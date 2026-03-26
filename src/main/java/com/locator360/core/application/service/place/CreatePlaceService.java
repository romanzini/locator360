package com.locator360.core.application.service.place;

import com.locator360.core.domain.place.Place;
import com.locator360.core.domain.place.PlaceAlertPolicy;
import com.locator360.core.domain.place.PlaceType;
import com.locator360.core.port.in.dto.input.CreatePlaceInputDto;
import com.locator360.core.port.in.dto.output.PlaceOutputDto;
import com.locator360.core.port.in.place.CreatePlaceUseCase;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.PlaceAlertPolicyRepository;
import com.locator360.core.port.out.PlaceRepository;
import io.micrometer.core.instrument.MeterRegistry;
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
public class CreatePlaceService implements CreatePlaceUseCase {

  private final PlaceRepository placeRepository;
  private final PlaceAlertPolicyRepository placeAlertPolicyRepository;
  private final CircleMemberRepository circleMemberRepository;
  private final ModelMapper modelMapper;
  private final MeterRegistry meterRegistry;

  @Override
  public PlaceOutputDto execute(UUID userId, UUID circleId, CreatePlaceInputDto input) {
    log.debug("Creating place for circle: {} by user: {}", circleId, userId);

    circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
        .orElseThrow(() -> new IllegalArgumentException("User is not a member of this circle"));

    PlaceType type = input.getType() != null ? PlaceType.valueOf(input.getType()) : null;

    Place place = Place.create(circleId, input.getName(), type, input.getAddressText(),
        input.getLatitude(), input.getLongitude(), input.getRadiusMeters(), userId);

    Place savedPlace = placeRepository.save(place);

    PlaceAlertPolicy policy = PlaceAlertPolicy.createDefault(savedPlace.getId(), circleId);
    placeAlertPolicyRepository.save(policy);

    meterRegistry.counter("places.created").increment();
    log.info("Place created: {} in circle: {}", savedPlace.getId(), circleId);

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
