package com.locator360.core.application.service.circle;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.circle.CircleRole;
import com.locator360.core.domain.circle.MemberStatus;
import com.locator360.core.domain.notification.NotificationCommand;
import com.locator360.core.domain.notification.NotificationType;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.NotificationCommandPublisher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferAdminServiceTest {

    @Mock
    private CircleMemberRepository circleMemberRepository;

    @Mock
    private NotificationCommandPublisher notificationCommandPublisher;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private TransferAdminService transferAdminService;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        private UUID adminId;
        private UUID circleId;
        private UUID newAdminId;
        private CircleMember currentAdminMember;
        private CircleMember newAdminMember;

        @BeforeEach
        void setUp() {
            adminId = UUID.randomUUID();
            circleId = UUID.randomUUID();
            newAdminId = UUID.randomUUID();

            currentAdminMember = CircleMember.restore(
                    UUID.randomUUID(), circleId, adminId,
                    CircleRole.ADMIN, MemberStatus.ACTIVE,
                    Instant.now(), null, Instant.now(), Instant.now());

            newAdminMember = CircleMember.restore(
                    UUID.randomUUID(), circleId, newAdminId,
                    CircleRole.MEMBER, MemberStatus.ACTIVE,
                    Instant.now(), null, Instant.now(), Instant.now());

            lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
            lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        }

        @Test
        @DisplayName("should transfer admin role successfully")
        void shouldTransferAdminRoleSuccessfully() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, adminId))
                    .thenReturn(Optional.of(currentAdminMember));
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, newAdminId))
                    .thenReturn(Optional.of(newAdminMember));
            when(circleMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(currentAdminMember, newAdminMember));

            transferAdminService.execute(adminId, circleId, newAdminId);

            assertEquals(CircleRole.MEMBER, currentAdminMember.getRole());
            assertEquals(CircleRole.ADMIN, newAdminMember.getRole());
            verify(circleMemberRepository, times(2)).save(any());
        }

        @Test
        @DisplayName("should throw when requester is not admin")
        void shouldThrowWhenRequesterIsNotAdmin() {
            CircleMember regularMember = CircleMember.restore(
                    UUID.randomUUID(), circleId, adminId,
                    CircleRole.MEMBER, MemberStatus.ACTIVE,
                    Instant.now(), null, Instant.now(), Instant.now());

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, adminId))
                    .thenReturn(Optional.of(regularMember));

            assertThrows(IllegalStateException.class,
                    () -> transferAdminService.execute(adminId, circleId, newAdminId));

            verify(circleMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when trying to transfer to self")
        void shouldThrowWhenTransferringToSelf() {
            assertThrows(IllegalArgumentException.class,
                    () -> transferAdminService.execute(adminId, circleId, adminId));

            verify(circleMemberRepository, never()).findByCircleIdAndUserId(any(), any());
        }

        @Test
        @DisplayName("should throw when admin not found in circle")
        void shouldThrowWhenAdminNotFoundInCircle() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, adminId))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> transferAdminService.execute(adminId, circleId, newAdminId));
        }

        @Test
        @DisplayName("should throw when new admin not found in circle")
        void shouldThrowWhenNewAdminNotFoundInCircle() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, adminId))
                    .thenReturn(Optional.of(currentAdminMember));
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, newAdminId))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> transferAdminService.execute(adminId, circleId, newAdminId));

            verify(circleMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when new admin member is not active")
        void shouldThrowWhenNewAdminIsNotActive() {
            CircleMember removedMember = CircleMember.restore(
                    UUID.randomUUID(), circleId, newAdminId,
                    CircleRole.MEMBER, MemberStatus.REMOVED,
                    Instant.now(), Instant.now(), Instant.now(), Instant.now());

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, adminId))
                    .thenReturn(Optional.of(currentAdminMember));
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, newAdminId))
                    .thenReturn(Optional.of(removedMember));

            assertThrows(IllegalStateException.class,
                    () -> transferAdminService.execute(adminId, circleId, newAdminId));

            verify(circleMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("should publish ADMIN_TRANSFERRED notification to all active members")
        void shouldPublishAdminTransferredNotification() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, adminId))
                    .thenReturn(Optional.of(currentAdminMember));
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, newAdminId))
                    .thenReturn(Optional.of(newAdminMember));
            when(circleMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(currentAdminMember, newAdminMember));

            transferAdminService.execute(adminId, circleId, newAdminId);

            ArgumentCaptor<NotificationCommand> captor = ArgumentCaptor.forClass(NotificationCommand.class);
            verify(notificationCommandPublisher, times(2)).publish(captor.capture());

            captor.getAllValues().forEach(cmd -> {
                assertEquals(NotificationType.ADMIN_TRANSFERRED, cmd.getType());
                assertEquals(circleId, cmd.getCircleId());
                assertEquals(newAdminId, cmd.getPayload().get("newAdminUserId"));
            });
        }
    }
}
