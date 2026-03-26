package com.locator360.core.application.service.place;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.circle.CircleRole;
import com.locator360.core.domain.circle.MemberStatus;
import com.locator360.core.domain.location.Location;
import com.locator360.core.domain.location.LocationSource;
import com.locator360.core.domain.notification.NotificationCommand;
import com.locator360.core.domain.notification.NotificationType;
import com.locator360.core.domain.place.*;
import com.locator360.core.domain.service.GeofenceDetectionService;
import com.locator360.core.port.out.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeofenceProcessingServiceTest {

    @Mock
    private GeofenceQueryPort geofenceQueryPort;

    @Mock
    private PlaceEventRepository placeEventRepository;

    @Mock
    private PlaceAlertPolicyRepository placeAlertPolicyRepository;

    @Mock
    private PlaceAlertTargetRepository placeAlertTargetRepository;

    @Mock
    private GeofenceEventPublisher geofenceEventPublisher;

    @Mock
    private NotificationCommandPublisher notificationCommandPublisher;

    @Mock
    private CircleMemberRepository circleMemberRepository;

    @Mock
    private GeofenceDetectionService geofenceDetectionService;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @Mock
    private Timer timer;

    @Mock
    private Timer.Sample timerSample;

    @InjectMocks
    private GeofenceProcessingService geofenceProcessingService;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        private UUID userId;
        private UUID circleId;
        private UUID placeId;
        private Location location;
        private Place place;
        private CircleMember member;
        private PlaceAlertPolicy policy;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            circleId = UUID.randomUUID();
            placeId = UUID.randomUUID();

            location = Location.create(userId, circleId, -23.5505, -46.6333,
                    10.0, 0.0, null, null, LocationSource.GPS, Instant.now(), false, 80);

            place = Place.restore(placeId, circleId, "Escola", PlaceType.SCHOOL,
                    "Rua A", -23.5505, -46.6333, 100.0, true,
                    UUID.randomUUID(), Instant.now(), Instant.now());

            member = CircleMember.restore(UUID.randomUUID(), circleId, userId,
                    CircleRole.MEMBER, MemberStatus.ACTIVE, Instant.now(), null,
                    Instant.now(), Instant.now());

            policy = PlaceAlertPolicy.createDefault(placeId, circleId);

            lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
            lenient().when(meterRegistry.timer(anyString(), any(String[].class))).thenReturn(timer);
        }

        @Test
        @DisplayName("should detect ENTER when user enters a geofence for the first time")
        void shouldDetectEnterWhenFirstTime() {
            when(circleMemberRepository.findByUserId(userId)).thenReturn(List.of(member));
            when(geofenceQueryPort.findPlacesNearPoint(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(List.of(place));
            when(geofenceDetectionService.isInsideGeofence(anyDouble(), anyDouble(), any(Place.class)))
                    .thenReturn(true);
            when(placeEventRepository.findLastByPlaceIdAndUserId(placeId, userId))
                    .thenReturn(Optional.empty());
            when(geofenceDetectionService.determineTransition(false, true))
                    .thenReturn(Optional.of(PlaceEventType.ENTER));
            when(placeEventRepository.save(any(PlaceEvent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(placeAlertPolicyRepository.findByPlaceId(placeId))
                    .thenReturn(Optional.of(policy));
            when(geofenceDetectionService.isPolicyActive(any(), any())).thenReturn(true);
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(member));

            geofenceProcessingService.execute(location);

            verify(placeEventRepository).save(any(PlaceEvent.class));
            verify(geofenceEventPublisher).publish(any(PlaceEvent.class));

            ArgumentCaptor<PlaceEvent> captor = ArgumentCaptor.forClass(PlaceEvent.class);
            verify(placeEventRepository).save(captor.capture());
            assertEquals(PlaceEventType.ENTER, captor.getValue().getEventType());
        }

        @Test
        @DisplayName("should detect EXIT when user leaves a geofence")
        void shouldDetectExitWhenLeavingGeofence() {
            PlaceEvent lastEnter = PlaceEvent.restore(UUID.randomUUID(), placeId, circleId,
                    userId, PlaceEventType.ENTER, null, Instant.now().minusSeconds(600), Instant.now());

            when(circleMemberRepository.findByUserId(userId)).thenReturn(List.of(member));
            when(geofenceQueryPort.findPlacesNearPoint(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(List.of(place));
            when(geofenceDetectionService.isInsideGeofence(anyDouble(), anyDouble(), any(Place.class)))
                    .thenReturn(false);
            when(placeEventRepository.findLastByPlaceIdAndUserId(placeId, userId))
                    .thenReturn(Optional.of(lastEnter));
            when(geofenceDetectionService.determineTransition(true, false))
                    .thenReturn(Optional.of(PlaceEventType.EXIT));
            when(placeEventRepository.save(any(PlaceEvent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(placeAlertPolicyRepository.findByPlaceId(placeId))
                    .thenReturn(Optional.of(policy));
            when(geofenceDetectionService.isPolicyActive(any(), any())).thenReturn(true);
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(member));

            geofenceProcessingService.execute(location);

            ArgumentCaptor<PlaceEvent> captor = ArgumentCaptor.forClass(PlaceEvent.class);
            verify(placeEventRepository).save(captor.capture());
            assertEquals(PlaceEventType.EXIT, captor.getValue().getEventType());
            verify(geofenceEventPublisher).publish(any(PlaceEvent.class));
        }

        @Test
        @DisplayName("should not create event when no transition occurs")
        void shouldNotCreateEventWhenNoTransition() {
            PlaceEvent lastEnter = PlaceEvent.restore(UUID.randomUUID(), placeId, circleId,
                    userId, PlaceEventType.ENTER, null, Instant.now().minusSeconds(60), Instant.now());

            when(circleMemberRepository.findByUserId(userId)).thenReturn(List.of(member));
            when(geofenceQueryPort.findPlacesNearPoint(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(List.of(place));
            when(geofenceDetectionService.isInsideGeofence(anyDouble(), anyDouble(), any(Place.class)))
                    .thenReturn(true);
            when(placeEventRepository.findLastByPlaceIdAndUserId(placeId, userId))
                    .thenReturn(Optional.of(lastEnter));
            when(geofenceDetectionService.determineTransition(true, true))
                    .thenReturn(Optional.empty());

            geofenceProcessingService.execute(location);

            verify(placeEventRepository, never()).save(any());
            verify(geofenceEventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("should not create event when no places are nearby")
        void shouldNotCreateEventWhenNoPlacesNearby() {
            when(circleMemberRepository.findByUserId(userId)).thenReturn(List.of(member));
            when(geofenceQueryPort.findPlacesNearPoint(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(List.of());

            geofenceProcessingService.execute(location);

            verify(placeEventRepository, never()).save(any());
            verify(geofenceEventPublisher, never()).publish(any());
            verify(notificationCommandPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("should not send notification when policy is inactive")
        void shouldNotSendNotificationWhenPolicyInactive() {
            when(circleMemberRepository.findByUserId(userId)).thenReturn(List.of(member));
            when(geofenceQueryPort.findPlacesNearPoint(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(List.of(place));
            when(geofenceDetectionService.isInsideGeofence(anyDouble(), anyDouble(), any(Place.class)))
                    .thenReturn(true);
            when(placeEventRepository.findLastByPlaceIdAndUserId(placeId, userId))
                    .thenReturn(Optional.empty());
            when(geofenceDetectionService.determineTransition(false, true))
                    .thenReturn(Optional.of(PlaceEventType.ENTER));
            when(placeEventRepository.save(any(PlaceEvent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(placeAlertPolicyRepository.findByPlaceId(placeId))
                    .thenReturn(Optional.of(policy));
            when(geofenceDetectionService.isPolicyActive(any(), any())).thenReturn(false);

            geofenceProcessingService.execute(location);

            verify(placeEventRepository).save(any(PlaceEvent.class));
            verify(geofenceEventPublisher).publish(any(PlaceEvent.class));
            verify(notificationCommandPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("should send notification to all circle members when target type is ALL_MEMBERS")
        void shouldSendNotificationToAllMembers() {
            CircleMember otherMember = CircleMember.restore(UUID.randomUUID(), circleId,
                    UUID.randomUUID(), CircleRole.ADMIN, MemberStatus.ACTIVE,
                    Instant.now(), null, Instant.now(), Instant.now());

            when(circleMemberRepository.findByUserId(userId)).thenReturn(List.of(member));
            when(geofenceQueryPort.findPlacesNearPoint(anyDouble(), anyDouble(), anyList()))
                    .thenReturn(List.of(place));
            when(geofenceDetectionService.isInsideGeofence(anyDouble(), anyDouble(), any(Place.class)))
                    .thenReturn(true);
            when(placeEventRepository.findLastByPlaceIdAndUserId(placeId, userId))
                    .thenReturn(Optional.empty());
            when(geofenceDetectionService.determineTransition(false, true))
                    .thenReturn(Optional.of(PlaceEventType.ENTER));
            when(placeEventRepository.save(any(PlaceEvent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(placeAlertPolicyRepository.findByPlaceId(placeId))
                    .thenReturn(Optional.of(policy));
            when(geofenceDetectionService.isPolicyActive(any(), any())).thenReturn(true);
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(member, otherMember));

            geofenceProcessingService.execute(location);

            // Should notify the other member (not the user who triggered the event)
            ArgumentCaptor<NotificationCommand> captor = ArgumentCaptor.forClass(NotificationCommand.class);
            verify(notificationCommandPublisher).publish(captor.capture());
            assertEquals(NotificationType.PLACE_ENTER, captor.getValue().getType());
            assertEquals(otherMember.getUserId(), captor.getValue().getRecipientUserId());
        }

        @Test
        @DisplayName("should not process when user has no circle memberships")
        void shouldNotProcessWhenNoMemberships() {
            when(circleMemberRepository.findByUserId(userId)).thenReturn(List.of());

            geofenceProcessingService.execute(location);

            verify(geofenceQueryPort, never()).findPlacesNearPoint(anyDouble(), anyDouble(), anyList());
        }
    }
}
