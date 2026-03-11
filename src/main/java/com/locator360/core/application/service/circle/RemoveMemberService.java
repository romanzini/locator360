package com.locator360.core.application.service.circle;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.notification.NotificationCommand;
import com.locator360.core.domain.notification.NotificationType;
import com.locator360.core.port.in.circle.RemoveMemberUseCase;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.NotificationCommandPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RemoveMemberService implements RemoveMemberUseCase {

    private final CircleMemberRepository circleMemberRepository;
    private final NotificationCommandPublisher notificationCommandPublisher;
    private final MeterRegistry meterRegistry;

    @Override
    public void execute(UUID adminId, UUID circleId, UUID memberId) {
        log.debug("Admin: {} removing member: {} from circle: {}", adminId, memberId, circleId);

        CircleMember admin = circleMemberRepository.findByCircleIdAndUserId(circleId, adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found in this circle"));

        if (!admin.isAdmin()) {
            throw new IllegalStateException("Only admin members can remove members");
        }

        if (adminId.equals(memberId)) {
            throw new IllegalArgumentException("Admin cannot remove themselves");
        }

        CircleMember member = circleMemberRepository.findByCircleIdAndUserId(circleId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found in this circle"));

        member.remove();
        circleMemberRepository.save(member);

        publishMemberRemovedNotifications(circleId, memberId);

        meterRegistry.counter("circles.members.removed").increment();
        log.info("Member: {} removed from circle: {} by admin: {}", memberId, circleId, adminId);
    }

    private void publishMemberRemovedNotifications(UUID circleId, UUID removedMemberUserId) {
        circleMemberRepository.findActiveByCircleId(circleId).forEach(member -> {
            NotificationCommand command = NotificationCommand.create(
                    NotificationType.MEMBER_REMOVED,
                    member.getUserId(),
                    circleId,
                    Map.of("removedMemberUserId", removedMemberUserId, "circleId", circleId));
            notificationCommandPublisher.publish(command);
        });
    }
}
