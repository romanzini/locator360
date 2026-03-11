package com.locator360.core.application.service.circle;

import com.locator360.core.domain.circle.*;
import com.locator360.core.domain.notification.NotificationCommand;
import com.locator360.core.domain.notification.NotificationType;
import com.locator360.core.port.in.dto.input.JoinCircleInputDto;
import com.locator360.core.port.in.dto.output.CircleMemberOutputDto;
import com.locator360.core.port.out.CircleInviteRepository;
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
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JoinCircleServiceTest {

    @Mock
    private CircleInviteRepository circleInviteRepository;

    @Mock
    private CircleMemberRepository circleMemberRepository;

    @Mock
    private CircleMembershipService circleMembershipService;

    @Mock
    private NotificationCommandPublisher notificationCommandPublisher;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private JoinCircleService joinCircleService;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        private UUID userId;
        private UUID circleId;
        private String inviteCode;
        private JoinCircleInputDto validInput;
        private CircleInvite pendingInvite;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            circleId = UUID.randomUUID();
            inviteCode = "ABCD1234";
            validInput = new JoinCircleInputDto(inviteCode);

            Instant futureExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
            pendingInvite = CircleInvite.restore(
                    UUID.randomUUID(), circleId, UUID.randomUUID(),
                    "invited@email.com", null, inviteCode,
                    InviteStatus.PENDING, null, futureExpiry,
                    Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS));

            lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
            lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
        }

        @Test
        @DisplayName("should throw exception when invite code not found")
        void shouldThrowExceptionWhenInviteCodeNotFound() {
            when(circleInviteRepository.findByInviteCode(inviteCode)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> joinCircleService.execute(userId, validInput));
        }

        @Test
        @DisplayName("should throw exception when invite status is ACCEPTED")
        void shouldThrowExceptionWhenInviteIsAccepted() {
            CircleInvite acceptedInvite = CircleInvite.restore(
                    UUID.randomUUID(), circleId, UUID.randomUUID(),
                    "invited@email.com", null, inviteCode,
                    InviteStatus.ACCEPTED, UUID.randomUUID(),
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS));

            when(circleInviteRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(acceptedInvite));

            assertThrows(IllegalStateException.class,
                    () -> joinCircleService.execute(userId, validInput));
        }

        @Test
        @DisplayName("should throw exception when invite status is EXPIRED")
        void shouldThrowExceptionWhenInviteIsExpired() {
            CircleInvite expiredInvite = CircleInvite.restore(
                    UUID.randomUUID(), circleId, UUID.randomUUID(),
                    "invited@email.com", null, inviteCode,
                    InviteStatus.EXPIRED, null,
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS));

            when(circleInviteRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(expiredInvite));

            assertThrows(IllegalStateException.class,
                    () -> joinCircleService.execute(userId, validInput));
        }

        @Test
        @DisplayName("should throw exception when invite expiresAt is in the past")
        void shouldThrowExceptionWhenInviteIsTimedOut() {
            CircleInvite timedOutInvite = CircleInvite.restore(
                    UUID.randomUUID(), circleId, UUID.randomUUID(),
                    "invited@email.com", null, inviteCode,
                    InviteStatus.PENDING, null,
                    Instant.now().minus(1, ChronoUnit.HOURS),
                    Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS));

            when(circleInviteRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(timedOutInvite));

            assertThrows(IllegalStateException.class,
                    () -> joinCircleService.execute(userId, validInput));
        }

        @Test
        @DisplayName("should throw exception when member limit is reached")
        void shouldThrowExceptionWhenMemberLimitReached() {
            when(circleInviteRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(pendingInvite));
            when(circleMemberRepository.countByCircleId(circleId)).thenReturn(5L);
            doThrow(new IllegalStateException("Circle has reached the maximum number of members (5)"))
                    .when(circleMembershipService).validateMemberLimit(5L);

            assertThrows(IllegalStateException.class,
                    () -> joinCircleService.execute(userId, validInput));
        }

        @Test
        @DisplayName("should create circle member on happy path")
        void shouldCreateCircleMemberOnHappyPath() {
            CircleMember existingMember1 = CircleMember.createAdmin(circleId, UUID.randomUUID());
            CircleMember existingMember2 = CircleMember.createMember(circleId, UUID.randomUUID());

            when(circleInviteRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(pendingInvite));
            when(circleMemberRepository.countByCircleId(circleId)).thenReturn(2L);
            when(circleMemberRepository.save(any(CircleMember.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleInviteRepository.save(any(CircleInvite.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(existingMember1, existingMember2));

            CircleMemberOutputDto mappedOutput = CircleMemberOutputDto.builder()
                    .id(UUID.randomUUID())
                    .circleId(circleId)
                    .userId(userId)
                    .role("MEMBER")
                    .status("ACTIVE")
                    .joinedAt(Instant.now())
                    .build();
            when(modelMapper.map(any(CircleMember.class), eq(CircleMemberOutputDto.class)))
                    .thenReturn(mappedOutput);

            CircleMemberOutputDto result = joinCircleService.execute(userId, validInput);

            assertNotNull(result);
            assertEquals(circleId, result.getCircleId());
            assertEquals("MEMBER", result.getRole());
            assertEquals("ACTIVE", result.getStatus());

            verify(circleMemberRepository).save(any(CircleMember.class));
        }

        @Test
        @DisplayName("should update invite to ACCEPTED on happy path")
        void shouldUpdateInviteToAcceptedOnHappyPath() {
            CircleMember existingMember = CircleMember.createAdmin(circleId, UUID.randomUUID());

            when(circleInviteRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(pendingInvite));
            when(circleMemberRepository.countByCircleId(circleId)).thenReturn(1L);
            when(circleMemberRepository.save(any(CircleMember.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.findActiveByCircleId(circleId)).thenReturn(List.of(existingMember));

            CircleMemberOutputDto mappedOutput = CircleMemberOutputDto.builder()
                    .id(UUID.randomUUID()).circleId(circleId).userId(userId)
                    .role("MEMBER").status("ACTIVE").joinedAt(Instant.now()).build();
            when(modelMapper.map(any(CircleMember.class), eq(CircleMemberOutputDto.class))).thenReturn(mappedOutput);

            joinCircleService.execute(userId, validInput);

            ArgumentCaptor<CircleInvite> inviteCaptor = ArgumentCaptor.forClass(CircleInvite.class);
            verify(circleInviteRepository).save(inviteCaptor.capture());
            assertEquals(InviteStatus.ACCEPTED, inviteCaptor.getValue().getStatus());
            assertEquals(userId, inviteCaptor.getValue().getAcceptedByUserId());
        }

        @Test
        @DisplayName("should publish MEMBER_JOINED to all active circle members except new member")
        void shouldPublishMemberJoinedToAllActiveMembersExceptNewMember() {
            UUID existingUserId1 = UUID.randomUUID();
            UUID existingUserId2 = UUID.randomUUID();
            CircleMember existingMember1 = CircleMember.createAdmin(circleId, existingUserId1);
            CircleMember newMemberAlreadySaved = CircleMember.restore(
                    UUID.randomUUID(), circleId, userId,
                    CircleRole.MEMBER, MemberStatus.ACTIVE,
                    Instant.now(), null, Instant.now(), Instant.now());
            CircleMember existingMember2 = CircleMember.createMember(circleId, existingUserId2);

            when(circleInviteRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(pendingInvite));
            when(circleMemberRepository.countByCircleId(circleId)).thenReturn(2L);
            when(circleMemberRepository.save(any(CircleMember.class))).thenReturn(newMemberAlreadySaved);
            when(circleInviteRepository.save(any(CircleInvite.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.findActiveByCircleId(circleId))
                    .thenReturn(List.of(existingMember1, newMemberAlreadySaved, existingMember2));

            CircleMemberOutputDto mappedOutput = CircleMemberOutputDto.builder()
                    .id(newMemberAlreadySaved.getId()).circleId(circleId).userId(userId)
                    .role("MEMBER").status("ACTIVE").joinedAt(Instant.now()).build();
            when(modelMapper.map(any(CircleMember.class), eq(CircleMemberOutputDto.class))).thenReturn(mappedOutput);

            joinCircleService.execute(userId, validInput);

            ArgumentCaptor<NotificationCommand> commandCaptor = ArgumentCaptor.forClass(NotificationCommand.class);
            verify(notificationCommandPublisher, times(2)).publish(commandCaptor.capture());

            List<NotificationCommand> commands = commandCaptor.getAllValues();
            assertTrue(commands.stream().allMatch(c -> c.getType() == NotificationType.MEMBER_JOINED));
            assertTrue(commands.stream().allMatch(c -> c.getCircleId().equals(circleId)));
            assertTrue(commands.stream().noneMatch(c -> c.getRecipientUserId().equals(userId)));
            assertTrue(commands.stream().anyMatch(c -> c.getRecipientUserId().equals(existingUserId1)));
            assertTrue(commands.stream().anyMatch(c -> c.getRecipientUserId().equals(existingUserId2)));
        }
    }
}
