package com.locator360.core.application.service.location;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.location.LocationSharingState;
import com.locator360.core.port.in.location.ResumeLocationSharingUseCase;
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
public class ResumeLocationSharingService implements ResumeLocationSharingUseCase {

  private final CircleMemberRepository circleMemberRepository;
  private final LocationSharingStateRepository locationSharingStateRepository;
  private final MeterRegistry meterRegistry;

  @Override
  public void execute(UUID userId, UUID circleId) {
    log.debug("Resuming location sharing for user: {} in circle: {}", userId, circleId);

    CircleMember member = circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
        .orElseThrow(() -> new IllegalArgumentException("User is not a member of this circle"));

    if (!member.isActive()) {
      throw new IllegalArgumentException("User is not an active member of this circle");
    }

    LocationSharingState sharingState = locationSharingStateRepository
        .findByUserIdAndCircleId(userId, circleId)
        .orElseGet(() -> LocationSharingState.create(userId, circleId));

    sharingState.resume();
    locationSharingStateRepository.save(sharingState);

    meterRegistry.counter("locations.sharing.resumed").increment();
    log.info("Location sharing resumed for user: {} in circle: {}", userId, circleId);
  }
}
