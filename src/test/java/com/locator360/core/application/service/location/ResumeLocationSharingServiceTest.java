package com.locator360.core.application.service.location;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.location.LocationSharingState;
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
class ResumeLocationSharingServiceTest {

  @Mock
  private CircleMemberRepository circleMemberRepository;

  @Mock
  private LocationSharingStateRepository locationSharingStateRepository;

  private ResumeLocationSharingService service;

  private UUID userId;
  private UUID circleId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    circleId = UUID.randomUUID();
    service = new ResumeLocationSharingService(
        circleMemberRepository,
        locationSharingStateRepository,
        new SimpleMeterRegistry());
  }

  @Nested
  @DisplayName("execute")
  class ExecuteTests {

    @Test
    @DisplayName("should resume sharing and clear pausedUntil")
    void shouldResumeSharingAndClearPausedUntil() {
      CircleMember member = CircleMember.createMember(circleId, userId);
      when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
          .thenReturn(Optional.of(member));

      LocationSharingState state = LocationSharingState.create(userId, circleId);
      state.pause(Instant.now().plus(1, ChronoUnit.HOURS));
      when(locationSharingStateRepository.findByUserIdAndCircleId(userId, circleId))
          .thenReturn(Optional.of(state));

      service.execute(userId, circleId);

      ArgumentCaptor<LocationSharingState> captor = ArgumentCaptor.forClass(LocationSharingState.class);
      verify(locationSharingStateRepository).save(captor.capture());

      LocationSharingState savedState = captor.getValue();
      assertTrue(savedState.isSharingLocation());
      assertNull(savedState.getPausedUntil());
    }

    @Test
    @DisplayName("should create state and keep sharing enabled when state does not exist")
    void shouldCreateStateAndKeepSharingEnabledWhenStateDoesNotExist() {
      CircleMember member = CircleMember.createMember(circleId, userId);
      when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
          .thenReturn(Optional.of(member));
      when(locationSharingStateRepository.findByUserIdAndCircleId(userId, circleId))
          .thenReturn(Optional.empty());

      service.execute(userId, circleId);

      ArgumentCaptor<LocationSharingState> captor = ArgumentCaptor.forClass(LocationSharingState.class);
      verify(locationSharingStateRepository).save(captor.capture());

      assertEquals(userId, captor.getValue().getUserId());
      assertEquals(circleId, captor.getValue().getCircleId());
      assertTrue(captor.getValue().isSharingLocation());
      assertNull(captor.getValue().getPausedUntil());
    }

    @Test
    @DisplayName("should throw when user is not a circle member")
    void shouldThrowWhenUserIsNotCircleMember() {
      when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
          .thenReturn(Optional.empty());

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
          () -> service.execute(userId, circleId));

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
          () -> service.execute(userId, circleId));

      assertEquals("User is not an active member of this circle", exception.getMessage());
      verify(locationSharingStateRepository, never()).save(any());
    }
  }
}
