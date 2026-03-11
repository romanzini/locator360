package com.locator360.core.application.service.circle;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.notification.NotificationCommand;
import com.locator360.core.domain.notification.NotificationType;
import com.locator360.core.port.in.circle.TransferAdminUseCase;
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
public class TransferAdminService implements TransferAdminUseCase {

    private final CircleMemberRepository circleMemberRepository;
    private final NotificationCommandPublisher notificationCommandPublisher;
    private final MeterRegistry meterRegistry;

    @Override
    public void execute(UUID adminId, UUID circleId, UUID newAdminId) {
        log.debug("Admin: {} transferring admin to: {} in circle: {}", adminId, newAdminId, circleId);

        if (adminId.equals(newAdminId)) {
            throw new IllegalArgumentException("Cannot transfer admin to yourself");
        }

        CircleMember currentAdmin = circleMemberRepository.findByCircleIdAndUserId(circleId, adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found in this circle"));

        if (!currentAdmin.isAdmin()) {
            throw new IllegalStateException("Only admin members can transfer admin role");
        }

        CircleMember newAdmin = circleMemberRepository.findByCircleIdAndUserId(circleId, newAdminId)
                .orElseThrow(() -> new IllegalArgumentException("New admin not found in this circle"));

        if (!newAdmin.isActive()) {
            throw new IllegalStateException("New admin must be an active member");
        }

        currentAdmin.demoteToMember();
        newAdmin.promoteToAdmin();

        circleMemberRepository.save(currentAdmin);
        circleMemberRepository.save(newAdmin);

        publishAdminTransferredNotifications(circleId, newAdminId);

        meterRegistry.counter("circles.admin.transferred").increment();
        log.info("Admin transferred from: {} to: {} in circle: {}", adminId, newAdminId, circleId);
    }

    private void publishAdminTransferredNotifications(UUID circleId, UUID newAdminUserId) {
        circleMemberRepository.findActiveByCircleId(circleId).forEach(member -> {
            NotificationCommand command = NotificationCommand.create(
                    NotificationType.ADMIN_TRANSFERRED,
                    member.getUserId(),
                    circleId,
                    Map.of("newAdminUserId", newAdminUserId, "circleId", circleId));
            notificationCommandPublisher.publish(command);
        });
    }
}
