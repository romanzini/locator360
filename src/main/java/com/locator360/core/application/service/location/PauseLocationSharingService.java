package com.locator360.core.application.service.location;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.location.LocationSharingState;
import com.locator360.core.port.in.dto.input.PauseLocationInputDto;
import com.locator360.core.port.in.location.PauseLocationSharingUseCase;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.LocationSharingStateRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PauseLocationSharingService implements PauseLocationSharingUseCase {

  private final CircleMemberRepository circleMemberRepository;
  private final LocationSharingStateRepository locationSharingStateRepository;
  private final MeterRegistry meterRegistry;

  @Override
  public void execute(UUID userId, UUID circleId, PauseLocationInputDto input) {
    if (input == null) {
      throw new IllegalArgumentException("Pause location input is required");
    }

    log.debug("Pausing location sharing for user: {} in circle: {} until: {}",
        userId, circleId, input.getPausedUntil());

    CircleMember member = circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
        .orElseThrow(() -> new IllegalArgumentException("User is not a member of this circle"));

    if (!member.isActive()) {
      throw new IllegalArgumentException("User is not an active member of this circle");
    }

    LocationSharingState sharingState = locationSharingStateRepository
        .findByUserIdAndCircleId(userId, circleId)
        .orElseGet(() -> LocationSharingState.create(userId, circleId));

    sharingState.pause(input.getPausedUntil());
    locationSharingStateRepository.save(sharingState);

    meterRegistry.counter("locations.sharing.paused").increment();
    log.info("Location sharing paused for user: {} in circle: {}", userId, circleId);
  }
}
