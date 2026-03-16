package com.locator360.core.application.service.location;

import com.locator360.core.domain.location.Location;
import com.locator360.core.domain.location.LocationSource;
import com.locator360.core.port.in.dto.input.LocationPointDto;
import com.locator360.core.port.in.dto.input.StreamLocationInputDto;
import com.locator360.core.port.in.location.StreamLocationUseCase;
import com.locator360.core.port.out.LastLocationCache;
import com.locator360.core.port.out.LocationEventPublisher;
import com.locator360.core.port.out.LocationRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StreamLocationService implements StreamLocationUseCase {

    private final LocationRepository locationRepository;
    private final LocationEventPublisher locationEventPublisher;
    private final LastLocationCache lastLocationCache;
    private final MeterRegistry meterRegistry;

    @Override
    public void execute(UUID userId, StreamLocationInputDto input) {
        Timer.Sample sample = Timer.start(meterRegistry);
        log.debug("Processing {} location events for user: {}", input.getEvents().size(), userId);

        List<Location> locations = input.getEvents().stream()
                .map(point -> toLocation(userId, input.getCircleId(), point))
                .collect(Collectors.toList());

        locationRepository.saveAll(locations);

        locationEventPublisher.publish(locations);

        locations.stream()
                .max(Comparator.comparing(Location::getRecordedAt))
                .ifPresent(latest -> lastLocationCache.save(userId, latest));

        meterRegistry.counter("locations.ingested").increment(locations.size());
        sample.stop(meterRegistry.timer("locations.ingestion.duration"));
        log.info("Processed {} location events for user: {}", locations.size(), userId);
    }

    private Location toLocation(UUID userId, UUID circleId, LocationPointDto point) {
        return Location.create(
                userId,
                circleId,
                point.getLatitude(),
                point.getLongitude(),
                point.getAccuracyMeters(),
                point.getSpeedMps(),
                point.getHeadingDegrees(),
                point.getAltitudeMeters(),
                LocationSource.valueOf(point.getSource()),
                point.getRecordedAt(),
                point.getIsMoving() != null && point.getIsMoving(),
                point.getBatteryLevel());
    }
}
