package com.locator360.core.application.service.circle;

import com.locator360.core.port.in.circle.ListCircleMembersUseCase;
import com.locator360.core.port.in.dto.output.CircleMemberOutputDto;
import com.locator360.core.port.out.CircleMemberRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ListCircleMembersService implements ListCircleMembersUseCase {

    private final CircleMemberRepository circleMemberRepository;
    private final ModelMapper modelMapper;
    private final MeterRegistry meterRegistry;

    @Override
    public List<CircleMemberOutputDto> execute(UUID userId, UUID circleId) {
        log.debug("User: {} listing members of circle: {}", userId, circleId);

        circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this circle"));

        List<CircleMemberOutputDto> members = circleMemberRepository.findActiveByCircleId(circleId).stream()
                .map(member -> modelMapper.map(member, CircleMemberOutputDto.class))
                .toList();

        log.debug("Found {} active members in circle: {}", members.size(), circleId);
        return members;
    }
}
