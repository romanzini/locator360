package com.locator360.core.application.service.place;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.location.Location;
import com.locator360.core.domain.notification.NotificationCommand;
import com.locator360.core.domain.notification.NotificationType;
import com.locator360.core.domain.place.*;
import com.locator360.core.domain.service.GeofenceDetectionService;
import com.locator360.core.port.in.place.ProcessGeofenceUseCase;
import com.locator360.core.port.out.*;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GeofenceProcessingService implements ProcessGeofenceUseCase {

        private final GeofenceQueryPort geofenceQueryPort;
        private final PlaceRepository placeRepository;
        private final PlaceEventRepository placeEventRepository;
        private final PlaceAlertPolicyRepository placeAlertPolicyRepository;
        private final PlaceAlertTargetRepository placeAlertTargetRepository;
        private final GeofenceEventPublisher geofenceEventPublisher;
        private final NotificationCommandPublisher notificationCommandPublisher;
        private final CircleMemberRepository circleMemberRepository;
        private final GeofenceDetectionService geofenceDetectionService;
        private final MeterRegistry meterRegistry;

    @Override
    public void execute(Location location) {
        log.debug("Processing geofence for user: {} at ({}, {})",
                location.getUserId(), location.getLatitude(), location.getLongitude());

        List<CircleMember> memberships = circleMemberRepository.findByUserId(location.getUserId());
        if (memberships.isEmpty()) {
            log.debug("User {} has no circle memberships, skipping geofence processing",
                    location.getUserId());
            return;
        }

        List<UUID> circleIds = memberships.stream()
                .map(CircleMember::getCircleId)
                .collect(Collectors.toList());

        // Buscar todos os lugares ativos dos círculos do usuário
        List<Place> allActivePlaces = circleIds.stream()
                .flatMap(circleId -> placeRepository.findActiveByCircleId(circleId).stream())
                .collect(Collectors.toList());

        if (allActivePlaces.isEmpty()) {
            log.debug("No active places for user: {}", location.getUserId());
            return;
        }

        for (Place place : allActivePlaces) {
            processPlaceGeofence(location, place);
        }
    }

    private void processPlaceGeofence(Location location, Place place) {
        boolean isNowInside = geofenceDetectionService.isInsideGeofence(
                location.getLatitude(), location.getLongitude(), place);

        Optional<PlaceEvent> lastEvent = placeEventRepository
                .findLastByPlaceIdAndUserId(place.getId(), location.getUserId());

        // Só considerar transição se o novo recordedAt for maior que o occurredAt do último evento
        if (lastEvent.isPresent() &&
                (location.getRecordedAt() == null || !location.getRecordedAt().isAfter(lastEvent.get().getOccurredAt()))) {
            log.debug("Ignoring geofence event for user: {} at place: {} due to non-increasing timestamp (last: {}, new: {})",
                    location.getUserId(), place.getId(), lastEvent.get().getOccurredAt(), location.getRecordedAt());
            return;
        }

        boolean wasInside = lastEvent
                .map(e -> e.getEventType() == PlaceEventType.ENTER)
                .orElse(false);

        Optional<PlaceEventType> transition = geofenceDetectionService
                .determineTransition(wasInside, isNowInside);

        if (transition.isEmpty()) {
            return;
        }

        PlaceEventType eventType = transition.get();
        PlaceEvent placeEvent = PlaceEvent.create(
                place.getId(), place.getCircleId(), location.getUserId(),
                eventType, location.getId(), location.getRecordedAt());

        placeEventRepository.save(placeEvent);
        geofenceEventPublisher.publish(placeEvent);

        meterRegistry.counter("geofence.events", "type", eventType.name()).increment();
        log.info("Geofence {} detected for user: {} at place: {}",
                eventType, location.getUserId(), place.getId());

        sendNotifications(placeEvent, place);
    }

    private void sendNotifications(PlaceEvent placeEvent, Place place) {
        Optional<PlaceAlertPolicy> policyOpt = placeAlertPolicyRepository
                .findByPlaceId(place.getId());

        if (policyOpt.isEmpty()) {
            return;
        }

        PlaceAlertPolicy policy = policyOpt.get();

        if (!shouldAlert(policy, placeEvent)) {
            return;
        }

        if (!geofenceDetectionService.isPolicyActive(policy, placeEvent.getOccurredAt())) {
            log.debug("Alert policy inactive for place: {} at time: {}",
                    place.getId(), placeEvent.getOccurredAt());
            return;
        }

        NotificationType notifType = placeEvent.getEventType() == PlaceEventType.ENTER
                ? NotificationType.PLACE_ENTER
                : NotificationType.PLACE_EXIT;

        List<UUID> recipientUserIds = resolveRecipients(policy, placeEvent);

        Map<String, Object> payload = Map.of(
                "placeId", place.getId().toString(),
                "placeName", place.getName(),
                "eventType", placeEvent.getEventType().name(),
                "userId", placeEvent.getUserId().toString()
        );

        for (UUID recipientId : recipientUserIds) {
            NotificationCommand command = NotificationCommand.create(
                    notifType, recipientId, place.getCircleId(), payload);
            notificationCommandPublisher.publish(command);
        }

        log.info("Sent {} notifications for {} event at place: {}",
                recipientUserIds.size(), placeEvent.getEventType(), place.getId());
    }

    private boolean shouldAlert(PlaceAlertPolicy policy, PlaceEvent placeEvent) {
        if (placeEvent.getEventType() == PlaceEventType.ENTER && !policy.isAlertOnEnter()) {
            return false;
        }
        return placeEvent.getEventType() != PlaceEventType.EXIT || policy.isAlertOnExit();
    }

    private List<UUID> resolveRecipients(PlaceAlertPolicy policy, PlaceEvent placeEvent) {
        if (policy.getTargetType() == TargetType.CUSTOM_LIST) {
            return placeAlertTargetRepository.findByPolicyId(policy.getId()).stream()
                    .map(PlaceAlertTarget::getUserId)
                    .filter(id -> !id.equals(placeEvent.getUserId()))
                    .collect(Collectors.toList());
        }

        List<CircleMember> members = circleMemberRepository
                .findActiveByCircleId(placeEvent.getCircleId());

        if (policy.getTargetType() == TargetType.ADMINS_ONLY) {
            return members.stream()
                    .filter(m -> m.getRole() == com.locator360.core.domain.circle.CircleRole.ADMIN)
                    .map(CircleMember::getUserId)
                    .filter(id -> !id.equals(placeEvent.getUserId()))
                    .collect(Collectors.toList());
        }

        // ALL_MEMBERS
        return members.stream()
                .map(CircleMember::getUserId)
                .filter(id -> !id.equals(placeEvent.getUserId()))
                .collect(Collectors.toList());
    }
}
