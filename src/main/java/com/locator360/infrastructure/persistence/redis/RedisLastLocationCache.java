package com.locator360.infrastructure.persistence.redis;

import com.locator360.core.domain.location.Location;
import com.locator360.core.domain.location.LocationSource;
import com.locator360.core.port.out.LastLocationCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLastLocationCache implements LastLocationCache {

    private static final String KEY_PREFIX = "last-location:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(UUID userId, Location location) {
        log.debug("Caching last location for user: {}", userId);
        String key = KEY_PREFIX + userId;
        Map<String, String> hash = toHash(location);
        redisTemplate.opsForHash().putAll(key, hash);
        log.debug("Last location cached for user: {}", userId);
    }

    @Override
    public Optional<Location> findByUserId(UUID userId) {
        log.debug("Retrieving cached last location for user: {}", userId);
        String key = KEY_PREFIX + userId;
        Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);
        if (hash.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromHash(hash));
    }

    private Map<String, String> toHash(Location location) {
        var map = new java.util.HashMap<String, String>();
        map.put("id", location.getId().toString());
        map.put("userId", location.getUserId().toString());
        map.put("latitude", String.valueOf(location.getLatitude()));
        map.put("longitude", String.valueOf(location.getLongitude()));
        map.put("source", location.getSource().name());
        map.put("recordedAt", location.getRecordedAt().toString());
        map.put("receivedAt", location.getReceivedAt().toString());
        map.put("isMoving", String.valueOf(location.isMoving()));
        map.put("createdAt", location.getCreatedAt().toString());
        if (location.getCircleId() != null) map.put("circleId", location.getCircleId().toString());
        if (location.getAccuracyMeters() != null) map.put("accuracyMeters", location.getAccuracyMeters().toString());
        if (location.getSpeedMps() != null) map.put("speedMps", location.getSpeedMps().toString());
        if (location.getHeadingDegrees() != null) map.put("headingDegrees", location.getHeadingDegrees().toString());
        if (location.getAltitudeMeters() != null) map.put("altitudeMeters", location.getAltitudeMeters().toString());
        if (location.getBatteryLevel() != null) map.put("batteryLevel", location.getBatteryLevel().toString());
        return map;
    }

    private Location fromHash(Map<Object, Object> hash) {
        String circleIdStr = (String) hash.get("circleId");
        String accuracyStr = (String) hash.get("accuracyMeters");
        String speedStr = (String) hash.get("speedMps");
        String headingStr = (String) hash.get("headingDegrees");
        String altitudeStr = (String) hash.get("altitudeMeters");
        String batteryStr = (String) hash.get("batteryLevel");

        return Location.restore(
                UUID.fromString((String) hash.get("id")),
                UUID.fromString((String) hash.get("userId")),
                circleIdStr != null ? UUID.fromString(circleIdStr) : null,
                Double.parseDouble((String) hash.get("latitude")),
                Double.parseDouble((String) hash.get("longitude")),
                accuracyStr != null ? Double.parseDouble(accuracyStr) : null,
                speedStr != null ? Double.parseDouble(speedStr) : null,
                headingStr != null ? Double.parseDouble(headingStr) : null,
                altitudeStr != null ? Double.parseDouble(altitudeStr) : null,
                LocationSource.valueOf((String) hash.get("source")),
                Instant.parse((String) hash.get("recordedAt")),
                Instant.parse((String) hash.get("receivedAt")),
                Boolean.parseBoolean((String) hash.get("isMoving")),
                batteryStr != null ? Integer.parseInt(batteryStr) : null,
                Instant.parse((String) hash.get("createdAt")));
    }
}
