package com.locator360.core.application.service.location;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.location.LocationSharingState;
import com.locator360.core.port.in.dto.input.PauseLocationInputDto;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.LocationSharingStateRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PauseLocationSharingServiceTest {

  @Mock
  private CircleMemberRepository circleMemberRepository;

  @Mock
  private LocationSharingStateRepository locationSharingStateRepository;

  private PauseLocationSharingService service;

  private UUID userId;
  private UUID circleId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    circleId = UUID.randomUUID();
    service = new PauseLocationSharingService(
        circleMemberRepository,
        locationSharingStateRepository,
        new SimpleMeterRegistry());
  }

  @Nested
  @DisplayName("execute")
  class ExecuteTests {

    @Test
    @DisplayName("should pause sharing with defined pausedUntil")
    void shouldPauseSharingWithDefinedPausedUntil() {
      Instant pausedUntil = Instant.now().plus(2, ChronoUnit.HOURS);
      PauseLocationInputDto input = new PauseLocationInputDto(pausedUntil);

      CircleMember member = CircleMember.createMember(circleId, userId);
      when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
          .thenReturn(Optional.of(member));

      LocationSharingState state = LocationSharingState.create(userId, circleId);
      when(locationSharingStateRepository.findByUserIdAndCircleId(userId, circleId))
          .thenReturn(Optional.of(state));

      service.execute(userId, circleId, input);

      ArgumentCaptor<LocationSharingState> captor = ArgumentCaptor.forClass(LocationSharingState.class);
      verify(locationSharingStateRepository).save(captor.capture());

      LocationSharingState savedState = captor.getValue();
      assertFalse(savedState.isSharingLocation());
      assertEquals(pausedUntil, savedState.getPausedUntil());
    }

    @Test
    @DisplayName("should pause sharing indefinitely when pausedUntil is null")
    void shouldPauseSharingIndefinitelyWhenPausedUntilIsNull() {
      PauseLocationInputDto input = new PauseLocationInputDto(null);

      CircleMember member = CircleMember.createMember(circleId, userId);
      when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
          .thenReturn(Optional.of(member));

      LocationSharingState state = LocationSharingState.create(userId, circleId);
      when(locationSharingStateRepository.findByUserIdAndCircleId(userId, circleId))
          .thenReturn(Optional.of(state));

      service.execute(userId, circleId, input);

      ArgumentCaptor<LocationSharingState> captor = ArgumentCaptor.forClass(LocationSharingState.class);
      verify(locationSharingStateRepository).save(captor.capture());

      LocationSharingState savedState = captor.getValue();
      assertFalse(savedState.isSharingLocation());
      assertNull(savedState.getPausedUntil());
    }

    @Test
    @DisplayName("should create state and pause when sharing state does not exist")
    void shouldCreateStateAndPauseWhenSharingStateDoesNotExist() {
      Instant pausedUntil = Instant.now().plus(1, ChronoUnit.HOURS);
      PauseLocationInputDto input = new PauseLocationInputDto(pausedUntil);

      CircleMember member = CircleMember.createMember(circleId, userId);
      when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
          .thenReturn(Optional.of(member));
      when(locationSharingStateRepository.findByUserIdAndCircleId(userId, circleId))
          .thenReturn(Optional.empty());

      service.execute(userId, circleId, input);

      ArgumentCaptor<LocationSharingState> captor = ArgumentCaptor.forClass(LocationSharingState.class);
      verify(locationSharingStateRepository).save(captor.capture());

      assertEquals(userId, captor.getValue().getUserId());
      assertEquals(circleId, captor.getValue().getCircleId());
      assertFalse(captor.getValue().isSharingLocation());
      assertEquals(pausedUntil, captor.getValue().getPausedUntil());
    }

    @Test
    @DisplayName("should throw when user is not a circle member")
    void shouldThrowWhenUserIsNotCircleMember() {
      when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
          .thenReturn(Optional.empty());

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> service.execute(userId, circleId, new PauseLocationInputDto(null)));

      assertEquals("User is not a member of this circle", exception.getMessage());
      verify(locationSharingStateRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when user is not an active circle member")
    void shouldThrowWhenUserIsNotActiveCircleMember() {
      CircleMember member = CircleMember.createMember(circleId, userId);
      member.remove();
      when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
          .thenReturn(Optional.of(member));

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> service.execute(userId, circleId, new PauseLocationInputDto(null)));

      assertEquals("User is not an active member of this circle", exception.getMessage());
      verify(locationSharingStateRepository, never()).save(any());
    }
  }
}
