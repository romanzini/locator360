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
class RemoveMemberServiceTest {

    @Mock
    private CircleMemberRepository circleMemberRepository;

    @Mock
    private NotificationCommandPublisher notificationCommandPublisher;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private RemoveMemberService removeMemberService;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        private UUID adminId;
        private UUID circleId;
        private UUID memberId;
        private CircleMember adminMember;
        private CircleMember targetMember;

        @BeforeEach
        void setUp() {
            adminId = UUID.randomUUID();
            circleId = UUID.randomUUID();
            memberId = UUID.randomUUID();

            adminMember = CircleMember.restore(
                    UUID.randomUUID(), circleId, adminId,
                    CircleRole.ADMIN, MemberStatus.ACTIVE,
                    Instant.now(), null, Instant.now(), Instant.now());

            targetMember = CircleMember.restore(
                    UUID.randomUUID(), circleId, memberId,
                    CircleRole.MEMBER, MemberStatus.ACTIVE,
                    Instant.now(), null, Instant.now(), Instant.now());

            lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
            lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        }

        @Test
        @DisplayName("should remove member successfully when admin requests it")
        void shouldRemoveMemberWhenAdminRequests() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, adminId))
                    .thenReturn(Optional.of(adminMember));
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, memberId))
                    .thenReturn(Optional.of(targetMember));
            when(circleMemberRepository.save(any())).thenReturn(targetMember);
            when(circleMemberRepository.findActiveByCircleId(circleId)).thenReturn(List.of(adminMember));

            removeMemberService.execute(adminId, circleId, memberId);

            verify(circleMemberRepository).save(targetMember);
            assertEquals(MemberStatus.REMOVED, targetMember.getStatus());
            assertNotNull(targetMember.getLeftAt());
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
                    () -> removeMemberService.execute(adminId, circleId, memberId));

            verify(circleMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when admin tries to remove themselves")
        void shouldThrowWhenAdminTriesToRemoveThemselves() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, adminId))
                    .thenReturn(Optional.of(adminMember));

            assertThrows(IllegalArgumentException.class,
                    () -> removeMemberService.execute(adminId, circleId, adminId));

            verify(circleMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when admin is not found in circle")
        void shouldThrowWhenAdminNotFoundInCircle() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, adminId))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> removeMemberService.execute(adminId, circleId, memberId));
        }

        @Test
        @DisplayName("should throw when target member is not found in circle")
        void shouldThrowWhenTargetMemberNotFound() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, adminId))
                    .thenReturn(Optional.of(adminMember));
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, memberId))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> removeMemberService.execute(adminId, circleId, memberId));

            verify(circleMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("should publish MEMBER_REMOVED notification to remaining active members")
        void shouldPublishMemberRemovedToActiveMembers() {
            CircleMember otherMember = CircleMember.restore(
                    UUID.randomUUID(), circleId, UUID.randomUUID(),
                    CircleRole.MEMBER, MemberStatus.ACTIVE,
                    Instant.now(), null, Instant.now(), Instant.now());

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, adminId))
                    .thenReturn(Optional.of(adminMember));
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, memberId))
                    .thenReturn(Optional.of(targetMember));
            when(circleMemberRepository.save(any())).thenReturn(targetMember);
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(adminMember, otherMember));

            removeMemberService.execute(adminId, circleId, memberId);

            ArgumentCaptor<NotificationCommand> captor = ArgumentCaptor.forClass(NotificationCommand.class);
            verify(notificationCommandPublisher, times(2)).publish(captor.capture());

            captor.getAllValues().forEach(cmd -> {
                assertEquals(NotificationType.MEMBER_REMOVED, cmd.getType());
                assertEquals(circleId, cmd.getCircleId());
            });
        }
    }
}
