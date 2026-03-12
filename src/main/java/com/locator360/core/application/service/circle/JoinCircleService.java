package com.locator360.core.application.service.circle;

import com.locator360.core.domain.circle.CircleInvite;
import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.circle.CircleMembershipService;
import com.locator360.core.domain.notification.NotificationCommand;
import com.locator360.core.domain.notification.NotificationType;
import com.locator360.core.port.in.circle.JoinCircleUseCase;
import com.locator360.core.port.in.dto.input.JoinCircleInputDto;
import com.locator360.core.port.in.dto.output.CircleMemberOutputDto;
import com.locator360.core.port.out.CircleInviteRepository;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.NotificationCommandPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class JoinCircleService implements JoinCircleUseCase {

    private final CircleInviteRepository circleInviteRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final CircleMembershipService circleMembershipService;
    private final NotificationCommandPublisher notificationCommandPublisher;
    private final ModelMapper modelMapper;
    private final MeterRegistry meterRegistry;

    @Override
    public CircleMemberOutputDto execute(UUID userId, JoinCircleInputDto input) {
        log.debug("User: {} joining circle with invite code: {}", userId, input.getInviteCode());

        CircleInvite invite = circleInviteRepository.findByInviteCode(input.getInviteCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        if (!invite.isPending()) {
            throw new IllegalStateException("Invite is not in PENDING status");
        }

        if (invite.isExpired()) {
            throw new IllegalStateException("Invite has expired");
        }

        UUID circleId = invite.getCircleId();

        CircleMember savedMember = circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
                .map(existing -> {
                    if (existing.isActive()) {
                        throw new IllegalStateException("User is already an active member of this circle");
                    }
                    log.debug("Reactivating previous member: {} in circle: {}", userId, circleId);
                    existing.rejoin();
                    return circleMemberRepository.save(existing);
                })
                .orElseGet(() -> {
                    long currentMemberCount = circleMemberRepository.countByCircleId(circleId);
                    circleMembershipService.validateMemberLimit(currentMemberCount);
                    return circleMemberRepository.save(CircleMember.createMember(circleId, userId));
                });

        invite.accept(userId);
        circleInviteRepository.save(invite);

        publishMemberJoinedNotifications(circleId, savedMember.getUserId());

        meterRegistry.counter("circles.members.joined").increment();
        log.info("User: {} joined circle: {}", userId, circleId);

        return modelMapper.map(savedMember, CircleMemberOutputDto.class);
    }

    private void publishMemberJoinedNotifications(UUID circleId, UUID newMemberUserId) {
        circleMemberRepository.findActiveByCircleId(circleId).stream()
                .filter(member -> !member.getUserId().equals(newMemberUserId))
                .forEach(member -> {
                    NotificationCommand command = NotificationCommand.create(
                            NotificationType.MEMBER_JOINED,
                            member.getUserId(),
                            circleId,
                            Map.of("newMemberUserId", newMemberUserId, "circleId", circleId));
                    notificationCommandPublisher.publish(command);
                });
    }
}
