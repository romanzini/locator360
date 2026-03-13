package com.locator360.core.application.service.circle;

import com.locator360.core.domain.circle.Circle;
import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.circle.CircleRole;
import com.locator360.core.domain.circle.MemberStatus;
import com.locator360.core.domain.notification.NotificationCommand;
import com.locator360.core.domain.notification.NotificationType;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.CircleRepository;
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
class LeaveCircleServiceTest {

        @Mock
        private CircleMemberRepository circleMemberRepository;

        @Mock
        private CircleRepository circleRepository;

        @Mock
        private NotificationCommandPublisher notificationCommandPublisher;

        @Mock
        private MeterRegistry meterRegistry;

        @Mock
        private Counter counter;

        @InjectMocks
        private LeaveCircleService leaveCircleService;

        @Nested
        @DisplayName("execute")
        class ExecuteTests {

                private UUID userId;
                private UUID circleId;

                @BeforeEach
                void setUp() {
                        userId = UUID.randomUUID();
                        circleId = UUID.randomUUID();

                        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
                        lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
                }

                @Test
                @DisplayName("should leave circle successfully when user is a regular member")
                void shouldLeaveCircleWhenRegularMember() {
                        CircleMember member = CircleMember.restore(
                                        UUID.randomUUID(), circleId, userId,
                                        CircleRole.MEMBER, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        CircleMember adminMember = CircleMember.restore(
                                        UUID.randomUUID(), circleId, UUID.randomUUID(),
                                        CircleRole.ADMIN, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                                        .thenReturn(Optional.of(member));
                        when(circleMemberRepository.findActiveByCircleId(circleId))
                                        .thenReturn(List.of(member, adminMember));
                        when(circleMemberRepository.save(any())).thenReturn(member);

                        leaveCircleService.execute(userId, circleId);

                        verify(circleMemberRepository).save(member);
                        assertEquals(MemberStatus.REMOVED, member.getStatus());
                        assertNotNull(member.getLeftAt());
                }

                @Test
                @DisplayName("should throw when user is not found in circle")
                void shouldThrowWhenUserNotFoundInCircle() {
                        when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                                        .thenReturn(Optional.empty());

                        assertThrows(IllegalArgumentException.class,
                                        () -> leaveCircleService.execute(userId, circleId));

                        verify(circleMemberRepository, never()).save(any());
                }

                @Test
                @DisplayName("should throw when only admin tries to leave with other active members")
                void shouldThrowWhenOnlyAdminLeavesWithOtherMembers() {
                        CircleMember adminMember = CircleMember.restore(
                                        UUID.randomUUID(), circleId, userId,
                                        CircleRole.ADMIN, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        CircleMember otherMember = CircleMember.restore(
                                        UUID.randomUUID(), circleId, UUID.randomUUID(),
                                        CircleRole.MEMBER, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                                        .thenReturn(Optional.of(adminMember));
                        when(circleMemberRepository.findActiveByCircleId(circleId))
                                        .thenReturn(List.of(adminMember, otherMember));

                        assertThrows(IllegalStateException.class,
                                        () -> leaveCircleService.execute(userId, circleId));

                        verify(circleMemberRepository, never()).save(any());
                }

                @Test
                @DisplayName("should allow only admin to leave when no other active members exist")
                void shouldAllowOnlyAdminToLeaveWhenNoOtherMembers() {
                        CircleMember adminMember = CircleMember.restore(
                                        UUID.randomUUID(), circleId, userId,
                                        CircleRole.ADMIN, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        Circle circle = Circle.create("Família", null, null, null, null, userId);

                        when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                                        .thenReturn(Optional.of(adminMember));
                        when(circleMemberRepository.findActiveByCircleId(circleId))
                                        .thenReturn(List.of(adminMember));
                        when(circleMemberRepository.save(any())).thenReturn(adminMember);
                        when(circleRepository.findById(circleId)).thenReturn(Optional.of(circle));
                        when(circleRepository.save(any())).thenReturn(circle);

                        leaveCircleService.execute(userId, circleId);

                        verify(circleMemberRepository).save(adminMember);
                        assertEquals(MemberStatus.REMOVED, adminMember.getStatus());
                        verify(circleRepository).findById(circleId);
                        verify(circleRepository).save(circle);
                        assertTrue(circle.isDeleted());
                }

                @Test
                @DisplayName("should allow admin to leave when another admin exists")
                void shouldAllowAdminToLeaveWhenAnotherAdminExists() {
                        CircleMember adminMember = CircleMember.restore(
                                        UUID.randomUUID(), circleId, userId,
                                        CircleRole.ADMIN, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        CircleMember otherAdmin = CircleMember.restore(
                                        UUID.randomUUID(), circleId, UUID.randomUUID(),
                                        CircleRole.ADMIN, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                                        .thenReturn(Optional.of(adminMember));
                        when(circleMemberRepository.findActiveByCircleId(circleId))
                                        .thenReturn(List.of(adminMember, otherAdmin));
                        when(circleMemberRepository.save(any())).thenReturn(adminMember);

                        leaveCircleService.execute(userId, circleId);

                        verify(circleMemberRepository).save(adminMember);
                        assertEquals(MemberStatus.REMOVED, adminMember.getStatus());
                }

                @Test
                @DisplayName("should publish MEMBER_LEFT notification to remaining active members")
                void shouldPublishMemberLeftToActiveMembers() {
                        CircleMember member = CircleMember.restore(
                                        UUID.randomUUID(), circleId, userId,
                                        CircleRole.MEMBER, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        UUID adminUserId = UUID.randomUUID();
                        CircleMember adminMember = CircleMember.restore(
                                        UUID.randomUUID(), circleId, adminUserId,
                                        CircleRole.ADMIN, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        UUID otherUserId = UUID.randomUUID();
                        CircleMember otherMember = CircleMember.restore(
                                        UUID.randomUUID(), circleId, otherUserId,
                                        CircleRole.MEMBER, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                                        .thenReturn(Optional.of(member));
                        when(circleMemberRepository.findActiveByCircleId(circleId))
                                        .thenReturn(List.of(member, adminMember, otherMember));
                        when(circleMemberRepository.save(any())).thenReturn(member);

                        leaveCircleService.execute(userId, circleId);

                        ArgumentCaptor<NotificationCommand> captor = ArgumentCaptor.forClass(NotificationCommand.class);
                        verify(notificationCommandPublisher, times(2)).publish(captor.capture());

                        captor.getAllValues().forEach(cmd -> {
                                assertEquals(NotificationType.MEMBER_LEFT, cmd.getType());
                                assertEquals(circleId, cmd.getCircleId());
                                assertNotEquals(userId, cmd.getRecipientUserId());
                        });
                }

                @Test
                @DisplayName("should not publish notifications when last member leaves")
                void shouldNotPublishNotificationsWhenLastMemberLeaves() {
                        CircleMember adminMember = CircleMember.restore(
                                        UUID.randomUUID(), circleId, userId,
                                        CircleRole.ADMIN, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        Circle circle = Circle.create("Família", null, null, null, null, userId);

                        when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                                        .thenReturn(Optional.of(adminMember));
                        when(circleMemberRepository.findActiveByCircleId(circleId))
                                        .thenReturn(List.of(adminMember));
                        when(circleMemberRepository.save(any())).thenReturn(adminMember);
                        when(circleRepository.findById(circleId)).thenReturn(Optional.of(circle));
                        when(circleRepository.save(any())).thenReturn(circle);

                        leaveCircleService.execute(userId, circleId);

                        verify(notificationCommandPublisher, never()).publish(any());
                }

                @Test
                @DisplayName("should increment circles.members.left metric")
                void shouldIncrementMetric() {
                        CircleMember member = CircleMember.restore(
                                        UUID.randomUUID(), circleId, userId,
                                        CircleRole.MEMBER, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        CircleMember adminMember = CircleMember.restore(
                                        UUID.randomUUID(), circleId, UUID.randomUUID(),
                                        CircleRole.ADMIN, MemberStatus.ACTIVE,
                                        Instant.now(), null, Instant.now(), Instant.now());

                        when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                                        .thenReturn(Optional.of(member));
                        when(circleMemberRepository.findActiveByCircleId(circleId))
                                        .thenReturn(List.of(member, adminMember));
                        when(circleMemberRepository.save(any())).thenReturn(member);

                        leaveCircleService.execute(userId, circleId);

                        verify(meterRegistry).counter("circles.members.left");
                        verify(counter).increment();
                }
        }
}
