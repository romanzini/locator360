package com.locator360.core.application.service.place;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.circle.CircleRole;
import com.locator360.core.domain.circle.MemberStatus;
import com.locator360.core.domain.place.Place;
import com.locator360.core.domain.place.PlaceType;
import com.locator360.core.port.in.dto.output.PlaceOutputDto;
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
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private CircleMemberRepository circleMemberRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private GetPlaceService getPlaceService;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        private UUID userId;
        private UUID circleId;
        private UUID placeId;
        private CircleMember activeMember;
        private Place existingPlace;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            circleId = UUID.randomUUID();
            placeId = UUID.randomUUID();
            activeMember = CircleMember.restore(UUID.randomUUID(), circleId, userId,
                    CircleRole.MEMBER, MemberStatus.ACTIVE, Instant.now(), null,
                    Instant.now(), Instant.now());
            existingPlace = Place.restore(placeId, circleId, "Casa", PlaceType.HOME,
                    "Rua A, 123", -23.5, -46.6, 100.0, true, userId,
                    Instant.now(), Instant.now());
        }

        @Test
        @DisplayName("should return place details")
        void shouldReturnPlaceDetails() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(activeMember));
            when(placeRepository.findById(placeId)).thenReturn(Optional.of(existingPlace));
            when(modelMapper.map(any(Place.class), eq(PlaceOutputDto.class)))
                    .thenReturn(PlaceOutputDto.builder()
                            .id(placeId)
                            .name("Casa")
                            .build());

            PlaceOutputDto result = getPlaceService.execute(userId, circleId, placeId);

            assertNotNull(result);
            assertEquals("Casa", result.getName());
        }

        @Test
        @DisplayName("should throw when user is not a member")
        void shouldThrowWhenUserNotMember() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    getPlaceService.execute(userId, circleId, placeId));
        }

        @Test
        @DisplayName("should throw when place not found")
        void shouldThrowWhenPlaceNotFound() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(activeMember));
            when(placeRepository.findById(placeId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    getPlaceService.execute(userId, circleId, placeId));
        }

        @Test
        @DisplayName("should throw when place belongs to another circle")
        void shouldThrowWhenPlaceBelongsToAnotherCircle() {
            UUID otherCircleId = UUID.randomUUID();
            Place otherPlace = Place.restore(placeId, otherCircleId, "Casa",
                    PlaceType.HOME, null, -23.5, -46.6, 100.0, true, userId,
                    Instant.now(), Instant.now());

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(activeMember));
            when(placeRepository.findById(placeId)).thenReturn(Optional.of(otherPlace));

            assertThrows(IllegalArgumentException.class, () ->
                    getPlaceService.execute(userId, circleId, placeId));
        }
    }
}
