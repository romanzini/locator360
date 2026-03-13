package com.locator360.core.application.service.circle;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.circle.CircleRole;
import com.locator360.core.domain.notification.NotificationCommand;
import com.locator360.core.domain.notification.NotificationType;
import com.locator360.core.port.in.circle.LeaveCircleUseCase;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.CircleRepository;
import com.locator360.core.port.out.NotificationCommandPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LeaveCircleService implements LeaveCircleUseCase {

        private final CircleMemberRepository circleMemberRepository;
        private final CircleRepository circleRepository;
        private final NotificationCommandPublisher notificationCommandPublisher;
        private final MeterRegistry meterRegistry;

        @Override
        public void execute(UUID userId, UUID circleId) {
                log.debug("User: {} leaving circle: {}", userId, circleId);

                CircleMember member = circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
                                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this circle"));

                List<CircleMember> activeMembers = circleMemberRepository.findActiveByCircleId(circleId);

                if (member.isAdmin()) {
                        long adminCount = activeMembers.stream()
                                        .filter(CircleMember::isAdmin)
                                        .count();

                        boolean hasOtherMembers = activeMembers.stream()
                                        .anyMatch(m -> !m.getUserId().equals(userId));

                        if (adminCount == 1 && hasOtherMembers) {
                                throw new IllegalStateException(
                                                "Cannot leave circle as the only admin. Transfer admin role first");
                        }
                }

                member.remove();
                circleMemberRepository.save(member);

                boolean isLastMember = activeMembers.stream()
                                .noneMatch(m -> !m.getUserId().equals(userId));

                if (isLastMember) {
                        circleRepository.findById(circleId).ifPresent(circle -> {
                                circle.delete();
                                circleRepository.save(circle);
                                log.info("Circle soft-deleted: {}", circleId);
                        });
                }

                publishMemberLeftNotifications(circleId, userId, activeMembers);

                meterRegistry.counter("circles.members.left").increment();
                log.info("User: {} left circle: {}", userId, circleId);
        }

        private void publishMemberLeftNotifications(UUID circleId, UUID leftUserId,
                        List<CircleMember> activeMembers) {
                activeMembers.stream()
                                .filter(m -> !m.getUserId().equals(leftUserId))
                                .forEach(member -> {
                                        NotificationCommand command = NotificationCommand.create(
                                                        NotificationType.MEMBER_LEFT,
                                                        member.getUserId(),
                                                        circleId,
                                                        Map.of("leftMemberUserId", leftUserId, "circleId", circleId));
                                        notificationCommandPublisher.publish(command);
                                });
        }
}
