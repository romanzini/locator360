package com.locator360.core.application.service.place;

import com.locator360.core.domain.place.Place;
import com.locator360.core.port.in.place.DeletePlaceUseCase;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.PlaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeletePlaceService implements DeletePlaceUseCase {

  private final PlaceRepository placeRepository;
  private final CircleMemberRepository circleMemberRepository;

  @Override
  public void execute(UUID userId, UUID circleId, UUID placeId) {
    log.debug("Deleting place: {} in circle: {} by user: {}", placeId, circleId, userId);

    circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
        .orElseThrow(() -> new IllegalArgumentException("User is not a member of this circle"));

    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> new IllegalArgumentException("Place not found"));

    if (!place.getCircleId().equals(circleId)) {
      throw new IllegalArgumentException("Place does not belong to this circle");
    }

    place.deactivate();
    placeRepository.save(place);
    log.info("Place deactivated: {} in circle: {}", placeId, circleId);
  }
}
