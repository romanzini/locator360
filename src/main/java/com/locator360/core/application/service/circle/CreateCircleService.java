package com.locator360.core.application.service.circle;

import com.locator360.core.domain.circle.*;
import com.locator360.core.port.in.circle.CreateCircleUseCase;
import com.locator360.core.port.in.dto.input.CreateCircleInputDto;
import com.locator360.core.port.in.dto.output.CircleOutputDto;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.CircleRepository;
import com.locator360.core.port.out.CircleSettingsRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CreateCircleService implements CreateCircleUseCase {

    private final CircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final CircleSettingsRepository circleSettingsRepository;
    private final ModelMapper modelMapper;
    private final MeterRegistry meterRegistry;

    @Override
    public CircleOutputDto execute(UUID userId, CreateCircleInputDto input) {
        log.debug("Creating circle for user: {}", userId);

        PrivacyLevel privacyLevel = input.getPrivacyLevel() != null
                ? PrivacyLevel.valueOf(input.getPrivacyLevel())
                : null;

        Circle circle = Circle.create(
                input.getName(),
                input.getDescription(),
                input.getPhotoUrl(),
                input.getColorHex(),
                privacyLevel,
                userId);

        Circle savedCircle = circleRepository.save(circle);

        CircleMember adminMember = CircleMember.createAdmin(savedCircle.getId(), userId);
        circleMemberRepository.save(adminMember);

        CircleSettings settings = CircleSettings.createDefault(savedCircle.getId());
        circleSettingsRepository.save(settings);

        meterRegistry.counter("circles.created").increment();
        log.info("Circle created: {}", savedCircle.getId());

        return modelMapper.map(savedCircle, CircleOutputDto.class)
                .toBuilder()
                .role(CircleRole.ADMIN.name())
                .build();
    }
}
