package com.locator360.core.domain.service;

import com.locator360.core.domain.place.Place;
import com.locator360.core.domain.place.PlaceAlertPolicy;
import com.locator360.core.domain.place.PlaceEventType;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeofenceDetectionService {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    public boolean isInsideGeofence(double latitude, double longitude, Place place) {
        double distance = haversineDistance(latitude, longitude,
                place.getLatitude(), place.getLongitude());
        return distance <= place.getRadiusMeters();
    }

    public Optional<PlaceEventType> determineTransition(boolean wasInside, boolean isNowInside) {
        if (!wasInside && isNowInside) {
            return Optional.of(PlaceEventType.ENTER);
        }
        if (wasInside && !isNowInside) {
            return Optional.of(PlaceEventType.EXIT);
        }
        return Optional.empty();
    }

    public boolean isPolicyActive(PlaceAlertPolicy policy, Instant eventTime) {
        if (policy.getDaysOfWeek() == null && policy.getStartTime() == null
                && policy.getEndTime() == null) {
            return true;
        }

        ZonedDateTime zdt = eventTime.atZone(ZoneOffset.UTC);

        if (policy.getDaysOfWeek() != null && !policy.getDaysOfWeek().isBlank()) {
            Set<DayOfWeek> activeDays = parseDaysOfWeek(policy.getDaysOfWeek());
            if (!activeDays.contains(zdt.getDayOfWeek())) {
                return false;
            }
        }

        if (policy.getStartTime() != null && policy.getEndTime() != null) {
            LocalTime eventLocalTime = zdt.toLocalTime();
            return !eventLocalTime.isBefore(policy.getStartTime())
                    && !eventLocalTime.isAfter(policy.getEndTime());
        }

        return true;
    }

    double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    private Set<DayOfWeek> parseDaysOfWeek(String daysOfWeek) {
        return Stream.of(daysOfWeek.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());
    }
}
