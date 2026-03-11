package com.locator360.core.application.service.circle;

import com.locator360.core.domain.circle.CircleInvite;
import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.circle.CircleMembershipService;
import com.locator360.core.port.in.circle.CreateInviteUseCase;
import com.locator360.core.port.in.dto.input.CreateInviteInputDto;
import com.locator360.core.port.in.dto.output.InviteOutputDto;
import com.locator360.core.port.out.CircleInviteRepository;
import com.locator360.core.port.out.CircleMemberRepository;
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
public class CreateInviteService implements CreateInviteUseCase {

    private final CircleMemberRepository circleMemberRepository;
    private final CircleInviteRepository circleInviteRepository;
    private final CircleMembershipService circleMembershipService;
    private final ModelMapper modelMapper;
    private final MeterRegistry meterRegistry;

    @Override
    public InviteOutputDto execute(UUID userId, UUID circleId, CreateInviteInputDto input) {
        log.debug("Creating invite for circle: {} by user: {}", circleId, userId);

        CircleMember member = circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this circle"));

        if (!member.isAdmin()) {
            throw new IllegalArgumentException("Only admin members can create invites");
        }

        long currentMemberCount = circleMemberRepository.countByCircleId(circleId);
        circleMembershipService.validateMemberLimit(currentMemberCount);

        CircleInvite invite = CircleInvite.create(
                circleId,
                userId,
                input.getTargetEmail(),
                input.getTargetPhone(),
                input.getExpiresAt());

        CircleInvite savedInvite = circleInviteRepository.save(invite);

        meterRegistry.counter("circles.invites.created").increment();
        log.info("Invite created for circle: {} with code: {}", circleId, savedInvite.getInviteCode());

        return modelMapper.map(savedInvite, InviteOutputDto.class);
    }
}
