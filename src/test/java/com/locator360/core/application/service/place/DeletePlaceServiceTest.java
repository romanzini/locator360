package com.locator360.core.application.service.place;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.circle.CircleRole;
import com.locator360.core.domain.circle.MemberStatus;
import com.locator360.core.domain.place.Place;
import com.locator360.core.domain.place.PlaceType;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletePlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private CircleMemberRepository circleMemberRepository;

    @InjectMocks
    private DeletePlaceService deletePlaceService;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        private UUID userId;
        private UUID circleId;
        private UUID placeId;
        private CircleMember adminMember;
        private Place existingPlace;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            circleId = UUID.randomUUID();
            placeId = UUID.randomUUID();
            adminMember = CircleMember.restore(UUID.randomUUID(), circleId, userId,
                    CircleRole.ADMIN, MemberStatus.ACTIVE, Instant.now(), null,
                    Instant.now(), Instant.now());
            existingPlace = Place.restore(placeId, circleId, "Casa", PlaceType.HOME,
                    null, -23.5, -46.6, 100.0, true, userId,
                    Instant.now(), Instant.now());
        }

        @Test
        @DisplayName("should deactivate place successfully")
        void shouldDeactivatePlaceSuccessfully() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(adminMember));
            when(placeRepository.findById(placeId)).thenReturn(Optional.of(existingPlace));
            when(placeRepository.save(any(Place.class))).thenAnswer(inv -> inv.getArgument(0));

            deletePlaceService.execute(userId, circleId, placeId);

            verify(placeRepository).save(any(Place.class));
        }

        @Test
        @DisplayName("should throw when user is not a member")
        void shouldThrowWhenUserNotMember() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    deletePlaceService.execute(userId, circleId, placeId));

            verify(placeRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when place not found")
        void shouldThrowWhenPlaceNotFound() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(adminMember));
            when(placeRepository.findById(placeId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    deletePlaceService.execute(userId, circleId, placeId));
        }

        @Test
        @DisplayName("should throw when place belongs to another circle")
        void shouldThrowWhenPlaceBelongsToAnotherCircle() {
            UUID otherCircleId = UUID.randomUUID();
            Place otherPlace = Place.restore(placeId, otherCircleId, "Casa",
                    PlaceType.HOME, null, -23.5, -46.6, 100.0, true, userId,
                    Instant.now(), Instant.now());

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(adminMember));
            when(placeRepository.findById(placeId)).thenReturn(Optional.of(otherPlace));

            assertThrows(IllegalArgumentException.class, () ->
                    deletePlaceService.execute(userId, circleId, placeId));
        }
    }
}
