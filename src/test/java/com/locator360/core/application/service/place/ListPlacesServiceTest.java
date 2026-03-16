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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListPlacesServiceTest {

  @Mock
  private PlaceRepository placeRepository;

  @Mock
  private CircleMemberRepository circleMemberRepository;

  @Mock
  private ModelMapper modelMapper;

  @InjectMocks
  private ListPlacesService listPlacesService;

  @Nested
  @DisplayName("execute")
  class ExecuteTests {

    private UUID userId;
    private UUID circleId;
    private CircleMember activeMember;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      circleId = UUID.randomUUID();
      activeMember = CircleMember.restore(UUID.randomUUID(), circleId, userId,
          CircleRole.MEMBER, MemberStatus.ACTIVE, Instant.now(), null,
          Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("should list active places for circle")
    void shouldListActivePlaces() {
      Place place1 = Place.restore(UUID.randomUUID(), circleId, "Casa",
          PlaceType.HOME, null, -23.5, -46.6, 100.0, true, userId,
          Instant.now(), Instant.now());
      Place place2 = Place.restore(UUID.randomUUID(), circleId, "Escola",
          PlaceType.SCHOOL, null, -23.4, -46.5, 150.0, true, userId,
          Instant.now(), Instant.now());

      when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
          .thenReturn(Optional.of(activeMember));
      when(placeRepository.findActiveByCircleId(circleId))
          .thenReturn(List.of(place1, place2));
      when(modelMapper.map(any(Place.class), eq(PlaceOutputDto.class)))
          .thenReturn(PlaceOutputDto.builder().name("Casa").build())
          .thenReturn(PlaceOutputDto.builder().name("Escola").build());

      List<PlaceOutputDto> result = listPlacesService.execute(userId, circleId);

      assertEquals(2, result.size());
      verify(placeRepository).findActiveByCircleId(circleId);
    }

    @Test
    @DisplayName("should return empty list when no places")
    void shouldReturnEmptyList() {
      when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
          .thenReturn(Optional.of(activeMember));
      when(placeRepository.findActiveByCircleId(circleId))
          .thenReturn(List.of());

      List<PlaceOutputDto> result = listPlacesService.execute(userId, circleId);

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should throw when user is not a member")
    void shouldThrowWhenUserNotMember() {
      when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
          .thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () -> listPlacesService.execute(userId, circleId));

      verify(placeRepository, never()).findActiveByCircleId(any());
    }
  }
}
