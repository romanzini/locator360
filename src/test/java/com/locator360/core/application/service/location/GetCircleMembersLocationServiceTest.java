package com.locator360.core.application.service.location;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.location.Location;
import com.locator360.core.domain.location.LocationSharingState;
import com.locator360.core.domain.location.LocationSource;
import com.locator360.core.domain.user.User;
import com.locator360.core.port.in.dto.output.MemberLocationOutputDto;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.LastLocationCache;
import com.locator360.core.port.out.LocationSharingStateRepository;
import com.locator360.core.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCircleMembersLocationServiceTest {

    @Mock
    private CircleMemberRepository circleMemberRepository;

    @Mock
    private LastLocationCache lastLocationCache;

    @Mock
    private LocationSharingStateRepository locationSharingStateRepository;

    @Mock
    private UserRepository userRepository;

    private GetCircleMembersLocationService service;

    private UUID requestingUserId;
    private UUID circleId;

    @BeforeEach
    void setUp() {
        requestingUserId = UUID.randomUUID();
        circleId = UUID.randomUUID();
        service = new GetCircleMembersLocationService(
                circleMemberRepository, lastLocationCache,
                locationSharingStateRepository, userRepository);
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("should throw when requesting user is not a member of the circle")
        void shouldThrowWhenUserNotMember() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, requestingUserId))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.execute(requestingUserId, circleId));

            verify(circleMemberRepository).findByCircleIdAndUserId(circleId, requestingUserId);
            verify(circleMemberRepository, never()).findActiveByCircleId(any());
        }

        @Test
        @DisplayName("should return empty list when no active members in circle")
        void shouldReturnEmptyWhenNoActiveMembers() {
            CircleMember requestingMember = CircleMember.createAdmin(circleId, requestingUserId);
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, requestingUserId))
                    .thenReturn(Optional.of(requestingMember));
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of());

            List<MemberLocationOutputDto> result = service.execute(requestingUserId, circleId);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return location for member who is sharing")
        void shouldReturnLocationForSharingMember() {
            UUID memberUserId = UUID.randomUUID();
            Instant locationTime = Instant.now().minus(1, ChronoUnit.MINUTES);

            CircleMember requestingMember = CircleMember.createAdmin(circleId, requestingUserId);
            CircleMember activeMember = CircleMember.createMember(circleId, memberUserId);

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, requestingUserId))
                    .thenReturn(Optional.of(requestingMember));
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(requestingMember, activeMember));

            LocationSharingState sharingState = LocationSharingState.create(memberUserId, circleId);
            when(locationSharingStateRepository.findByUserIdAndCircleId(memberUserId, circleId))
                    .thenReturn(Optional.of(sharingState));

            Location location = Location.restore(
                    UUID.randomUUID(), memberUserId, circleId,
                    -23.561414, -46.655881, 10.0, 1.5, 180.0, 760.0,
                    LocationSource.GPS, locationTime, Instant.now(),
                    true, 85, Instant.now());
            when(lastLocationCache.findByUserId(memberUserId))
                    .thenReturn(Optional.of(location));

            User memberUser = User.restore(
                    memberUserId, "member@test.com", null, "John Doe",
                    "John", "Doe", null, null, "https://photo.url/john.jpg",
                    "pt-BR", "America/Sao_Paulo", null, null, Instant.now(), Instant.now());
            when(userRepository.findById(memberUserId))
                    .thenReturn(Optional.of(memberUser));

            // Requesting user also needs user info
            LocationSharingState requestingSharingState = LocationSharingState.create(requestingUserId, circleId);
            when(locationSharingStateRepository.findByUserIdAndCircleId(requestingUserId, circleId))
                    .thenReturn(Optional.of(requestingSharingState));

            Location requestingLocation = Location.restore(
                    UUID.randomUUID(), requestingUserId, circleId,
                    -23.570000, -46.660000, 5.0, 0.0, 0.0, 750.0,
                    LocationSource.GPS, locationTime, Instant.now(),
                    false, 90, Instant.now());
            when(lastLocationCache.findByUserId(requestingUserId))
                    .thenReturn(Optional.of(requestingLocation));

            User requestingUser = User.restore(
                    requestingUserId, "requester@test.com", null, "Jane Smith",
                    "Jane", "Smith", null, null, null,
                    "pt-BR", "America/Sao_Paulo", null, null, Instant.now(), Instant.now());
            when(userRepository.findById(requestingUserId))
                    .thenReturn(Optional.of(requestingUser));

            List<MemberLocationOutputDto> result = service.execute(requestingUserId, circleId);

            assertEquals(2, result.size());

            MemberLocationOutputDto memberDto = result.stream()
                    .filter(dto -> dto.getUserId().equals(memberUserId))
                    .findFirst().orElseThrow();

            assertEquals("John Doe", memberDto.getFullName());
            assertEquals("https://photo.url/john.jpg", memberDto.getProfilePhotoUrl());
            assertEquals(-23.561414, memberDto.getLatitude());
            assertEquals(-46.655881, memberDto.getLongitude());
            assertEquals(10.0, memberDto.getAccuracy());
            assertEquals(1.5, memberDto.getSpeed());
            assertTrue(memberDto.getIsMoving());
            assertEquals(85, memberDto.getBatteryLevel());
            assertEquals(locationTime, memberDto.getLastUpdatedAt());
        }

        @Test
        @DisplayName("should skip member with paused sharing")
        void shouldSkipMemberWithPausedSharing() {
            UUID memberUserId = UUID.randomUUID();

            CircleMember requestingMember = CircleMember.createAdmin(circleId, requestingUserId);
            CircleMember pausedMember = CircleMember.createMember(circleId, memberUserId);

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, requestingUserId))
                    .thenReturn(Optional.of(requestingMember));
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(requestingMember, pausedMember));

            // Paused member
            LocationSharingState pausedState = LocationSharingState.create(memberUserId, circleId);
            pausedState.pause(Instant.now().plus(1, ChronoUnit.HOURS));
            when(locationSharingStateRepository.findByUserIdAndCircleId(memberUserId, circleId))
                    .thenReturn(Optional.of(pausedState));

            // Requesting user is sharing
            LocationSharingState requestingState = LocationSharingState.create(requestingUserId, circleId);
            when(locationSharingStateRepository.findByUserIdAndCircleId(requestingUserId, circleId))
                    .thenReturn(Optional.of(requestingState));

            Location requestingLocation = Location.restore(
                    UUID.randomUUID(), requestingUserId, circleId,
                    -23.570000, -46.660000, 5.0, 0.0, 0.0, 750.0,
                    LocationSource.GPS, Instant.now(), Instant.now(),
                    false, 90, Instant.now());
            when(lastLocationCache.findByUserId(requestingUserId))
                    .thenReturn(Optional.of(requestingLocation));

            User requestingUser = User.restore(
                    requestingUserId, "requester@test.com", null, "Jane Smith",
                    "Jane", "Smith", null, null, null,
                    "pt-BR", "America/Sao_Paulo", null, null, Instant.now(), Instant.now());
            when(userRepository.findById(requestingUserId))
                    .thenReturn(Optional.of(requestingUser));

            List<MemberLocationOutputDto> result = service.execute(requestingUserId, circleId);

            assertEquals(1, result.size());
            assertEquals(requestingUserId, result.get(0).getUserId());
            verify(lastLocationCache, never()).findByUserId(memberUserId);
        }

        @Test
        @DisplayName("should skip member without location in cache")
        void shouldSkipMemberWithoutCachedLocation() {
            UUID memberUserId = UUID.randomUUID();

            CircleMember requestingMember = CircleMember.createAdmin(circleId, requestingUserId);
            CircleMember memberWithoutLocation = CircleMember.createMember(circleId, memberUserId);

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, requestingUserId))
                    .thenReturn(Optional.of(requestingMember));
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(requestingMember, memberWithoutLocation));

            // Member is sharing but has no cached location
            LocationSharingState sharingState = LocationSharingState.create(memberUserId, circleId);
            when(locationSharingStateRepository.findByUserIdAndCircleId(memberUserId, circleId))
                    .thenReturn(Optional.of(sharingState));
            when(lastLocationCache.findByUserId(memberUserId))
                    .thenReturn(Optional.empty());

            // Requesting user also has no cached location
            LocationSharingState requestingState = LocationSharingState.create(requestingUserId, circleId);
            when(locationSharingStateRepository.findByUserIdAndCircleId(requestingUserId, circleId))
                    .thenReturn(Optional.of(requestingState));
            when(lastLocationCache.findByUserId(requestingUserId))
                    .thenReturn(Optional.empty());

            List<MemberLocationOutputDto> result = service.execute(requestingUserId, circleId);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should include member without sharing state (default is active)")
        void shouldIncludeMemberWithoutSharingState() {
            UUID memberUserId = UUID.randomUUID();
            Instant locationTime = Instant.now();

            CircleMember requestingMember = CircleMember.createAdmin(circleId, requestingUserId);
            CircleMember memberNoState = CircleMember.createMember(circleId, memberUserId);

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, requestingUserId))
                    .thenReturn(Optional.of(requestingMember));
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(requestingMember, memberNoState));

            // Neither member has a sharing state record
            when(locationSharingStateRepository.findByUserIdAndCircleId(memberUserId, circleId))
                    .thenReturn(Optional.empty());
            when(locationSharingStateRepository.findByUserIdAndCircleId(requestingUserId, circleId))
                    .thenReturn(Optional.empty());

            // Both have cached locations
            Location memberLocation = Location.restore(
                    UUID.randomUUID(), memberUserId, circleId,
                    -23.561414, -46.655881, 10.0, 1.5, 180.0, 760.0,
                    LocationSource.GPS, locationTime, Instant.now(),
                    true, 85, Instant.now());
            when(lastLocationCache.findByUserId(memberUserId))
                    .thenReturn(Optional.of(memberLocation));

            Location requestingLocation = Location.restore(
                    UUID.randomUUID(), requestingUserId, circleId,
                    -23.570000, -46.660000, 5.0, 0.0, 0.0, 750.0,
                    LocationSource.GPS, locationTime, Instant.now(),
                    false, 90, Instant.now());
            when(lastLocationCache.findByUserId(requestingUserId))
                    .thenReturn(Optional.of(requestingLocation));

            // Both have user records
            User memberUser = User.restore(
                    memberUserId, "member@test.com", null, "John Doe",
                    "John", "Doe", null, null, null,
                    "pt-BR", "America/Sao_Paulo", null, null, Instant.now(), Instant.now());
            when(userRepository.findById(memberUserId))
                    .thenReturn(Optional.of(memberUser));

            User requestingUser = User.restore(
                    requestingUserId, "requester@test.com", null, "Jane Smith",
                    "Jane", "Smith", null, null, null,
                    "pt-BR", "America/Sao_Paulo", null, null, Instant.now(), Instant.now());
            when(userRepository.findById(requestingUserId))
                    .thenReturn(Optional.of(requestingUser));

            List<MemberLocationOutputDto> result = service.execute(requestingUserId, circleId);

            assertEquals(2, result.size());
            verify(lastLocationCache).findByUserId(memberUserId);
            verify(lastLocationCache).findByUserId(requestingUserId);
        }

        @Test
        @DisplayName("should include multiple sharing members")
        void shouldIncludeMultipleSharingMembers() {
            UUID member1Id = UUID.randomUUID();
            UUID member2Id = UUID.randomUUID();
            Instant locationTime = Instant.now();

            CircleMember requestingMember = CircleMember.createAdmin(circleId, requestingUserId);
            CircleMember member1 = CircleMember.createMember(circleId, member1Id);
            CircleMember member2 = CircleMember.createMember(circleId, member2Id);

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, requestingUserId))
                    .thenReturn(Optional.of(requestingMember));
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(requestingMember, member1, member2));

            // All members sharing
            for (UUID memberId : List.of(requestingUserId, member1Id, member2Id)) {
                LocationSharingState state = LocationSharingState.create(memberId, circleId);
                when(locationSharingStateRepository.findByUserIdAndCircleId(memberId, circleId))
                        .thenReturn(Optional.of(state));

                Location loc = Location.restore(
                        UUID.randomUUID(), memberId, circleId,
                        -23.0 - memberId.hashCode() % 10 * 0.001, -46.0, 10.0, 0.0, 0.0, 750.0,
                        LocationSource.GPS, locationTime, Instant.now(),
                        false, 80, Instant.now());
                when(lastLocationCache.findByUserId(memberId))
                        .thenReturn(Optional.of(loc));

                User user = User.restore(
                        memberId, memberId + "@test.com", null, "User " + memberId.toString().substring(0, 4),
                        "User", memberId.toString().substring(0, 4), null, null, null,
                        "pt-BR", "America/Sao_Paulo", null, null, Instant.now(), Instant.now());
                when(userRepository.findById(memberId))
                        .thenReturn(Optional.of(user));
            }

            List<MemberLocationOutputDto> result = service.execute(requestingUserId, circleId);

            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("should throw when requesting user membership is not active")
        void shouldThrowWhenRequestingUserNotActive() {
            CircleMember removedMember = CircleMember.createMember(circleId, requestingUserId);
            removedMember.remove();

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, requestingUserId))
                    .thenReturn(Optional.of(removedMember));

            assertThrows(IllegalArgumentException.class,
                    () -> service.execute(requestingUserId, circleId));

            verify(circleMemberRepository, never()).findActiveByCircleId(any());
        }
    }
}
