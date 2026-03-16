package com.locator360.core.application.service.location;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.location.Location;
import com.locator360.core.domain.location.LocationSharingState;
import com.locator360.core.domain.user.User;
import com.locator360.core.port.in.dto.output.MemberLocationOutputDto;
import com.locator360.core.port.in.dto.output.SharingStatus;
import com.locator360.core.port.in.location.GetCircleMembersLocationUseCase;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.LastLocationCache;
import com.locator360.core.port.out.LocationSharingStateRepository;
import com.locator360.core.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GetCircleMembersLocationService implements GetCircleMembersLocationUseCase {

    private final CircleMemberRepository circleMemberRepository;
    private final LastLocationCache lastLocationCache;
    private final LocationSharingStateRepository locationSharingStateRepository;
    private final UserRepository userRepository;
    private final Duration staleThreshold;

    @Override
    public List<MemberLocationOutputDto> execute(UUID userId, UUID circleId) {
        log.debug("Getting circle members location for user: {} in circle: {}", userId, circleId);

        CircleMember requestingMember = circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this circle"));

        if (!requestingMember.isActive()) {
            throw new IllegalArgumentException("User is not an active member of this circle");
        }

        List<CircleMember> activeMembers = circleMemberRepository.findActiveByCircleId(circleId);

        List<MemberLocationOutputDto> result = new ArrayList<>();

        for (CircleMember member : activeMembers) {
            UUID memberUserId = member.getUserId();

            Optional<LocationSharingState> sharingState =
                    locationSharingStateRepository.findByUserIdAndCircleId(memberUserId, circleId);

            boolean isPaused = sharingState.isPresent() && !sharingState.get().isSharingActive();

            if (isPaused) {
                Optional<User> user = userRepository.findById(memberUserId);
                if (user.isEmpty()) {
                    continue;
                }
                result.add(MemberLocationOutputDto.builder()
                        .userId(memberUserId)
                        .fullName(user.get().getFullName())
                        .profilePhotoUrl(user.get().getProfilePhotoUrl())
                        .sharingStatus(SharingStatus.PAUSED)
                        .build());
                continue;
            }

            Optional<Location> lastLocation = lastLocationCache.findByUserId(memberUserId);
            if (lastLocation.isEmpty()) {
                continue;
            }

            Optional<User> user = userRepository.findById(memberUserId);
            if (user.isEmpty()) {
                continue;
            }

            Location location = lastLocation.get();
            User memberUser = user.get();

            SharingStatus status = determineSharingStatus(location.getRecordedAt());

            result.add(MemberLocationOutputDto.builder()
                    .userId(memberUserId)
                    .fullName(memberUser.getFullName())
                    .profilePhotoUrl(memberUser.getProfilePhotoUrl())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .accuracy(location.getAccuracyMeters())
                    .speed(location.getSpeedMps())
                    .isMoving(location.isMoving())
                    .batteryLevel(location.getBatteryLevel())
                    .lastUpdatedAt(location.getRecordedAt())
                    .sharingStatus(status)
                    .build());
        }

        log.info("Returned {} member locations for circle: {}", result.size(), circleId);
        return result;
    }

    private SharingStatus determineSharingStatus(Instant recordedAt) {
        if (recordedAt == null) {
            return SharingStatus.STALE;
        }
        return Duration.between(recordedAt, Instant.now()).compareTo(staleThreshold) > 0
                ? SharingStatus.STALE
                : SharingStatus.ONLINE;
    }
}
