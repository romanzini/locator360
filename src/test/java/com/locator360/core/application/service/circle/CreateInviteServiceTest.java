package com.locator360.core.application.service.circle;

import com.locator360.core.domain.circle.*;
import com.locator360.core.port.in.dto.input.CreateInviteInputDto;
import com.locator360.core.port.in.dto.output.InviteOutputDto;
import com.locator360.core.port.out.CircleInviteRepository;
import com.locator360.core.port.out.CircleMemberRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateInviteServiceTest {

    @Mock
    private CircleMemberRepository circleMemberRepository;

    @Mock
    private CircleInviteRepository circleInviteRepository;

    @Mock
    private CircleMembershipService circleMembershipService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private CreateInviteService createInviteService;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        private UUID userId;
        private UUID circleId;
        private CreateInviteInputDto validInput;
        private CircleMember adminMember;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            circleId = UUID.randomUUID();
            validInput = new CreateInviteInputDto("test@email.com", null, null);
            adminMember = CircleMember.createAdmin(circleId, userId);

            lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        }

        @Test
        @DisplayName("should create invite successfully")
        void shouldCreateInviteSuccessfully() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(adminMember));
            when(circleMemberRepository.countByCircleId(circleId)).thenReturn(2L);
            when(circleInviteRepository.save(any(CircleInvite.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            InviteOutputDto mappedOutput = InviteOutputDto.builder()
                    .id(UUID.randomUUID())
                    .circleId(circleId)
                    .inviteCode("ABC12345")
                    .status("PENDING")
                    .build();
            when(modelMapper.map(any(CircleInvite.class), eq(InviteOutputDto.class)))
                    .thenReturn(mappedOutput);

            InviteOutputDto result = createInviteService.execute(userId, circleId, validInput);

            assertNotNull(result);
            assertEquals(circleId, result.getCircleId());
            assertEquals("PENDING", result.getStatus());
        }

        @Test
        @DisplayName("should save invite via repository")
        void shouldSaveInviteViaRepository() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(adminMember));
            when(circleMemberRepository.countByCircleId(circleId)).thenReturn(2L);
            when(circleInviteRepository.save(any(CircleInvite.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(CircleInvite.class), eq(InviteOutputDto.class)))
                    .thenReturn(InviteOutputDto.builder().build());

            createInviteService.execute(userId, circleId, validInput);

            ArgumentCaptor<CircleInvite> captor = ArgumentCaptor.forClass(CircleInvite.class);
            verify(circleInviteRepository).save(captor.capture());

            CircleInvite savedInvite = captor.getValue();
            assertEquals(circleId, savedInvite.getCircleId());
            assertEquals(userId, savedInvite.getInvitedByUserId());
            assertEquals("test@email.com", savedInvite.getTargetEmail());
            assertEquals(InviteStatus.PENDING, savedInvite.getStatus());
            assertNotNull(savedInvite.getInviteCode());
        }

        @Test
        @DisplayName("should create invite with phone number")
        void shouldCreateInviteWithPhoneNumber() {
            CreateInviteInputDto phoneInput = new CreateInviteInputDto(null, "+5511999999999", null);

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(adminMember));
            when(circleMemberRepository.countByCircleId(circleId)).thenReturn(2L);
            when(circleInviteRepository.save(any(CircleInvite.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(CircleInvite.class), eq(InviteOutputDto.class)))
                    .thenReturn(InviteOutputDto.builder().build());

            createInviteService.execute(userId, circleId, phoneInput);

            ArgumentCaptor<CircleInvite> captor = ArgumentCaptor.forClass(CircleInvite.class);
            verify(circleInviteRepository).save(captor.capture());

            assertEquals("+5511999999999", captor.getValue().getTargetPhone());
            assertNull(captor.getValue().getTargetEmail());
        }

        @Test
        @DisplayName("should create invite with expiration date")
        void shouldCreateInviteWithExpiration() {
            Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
            CreateInviteInputDto inputWithExpiry = new CreateInviteInputDto("test@email.com", null, expiresAt);

            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(adminMember));
            when(circleMemberRepository.countByCircleId(circleId)).thenReturn(2L);
            when(circleInviteRepository.save(any(CircleInvite.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(CircleInvite.class), eq(InviteOutputDto.class)))
                    .thenReturn(InviteOutputDto.builder().build());

            createInviteService.execute(userId, circleId, inputWithExpiry);

            ArgumentCaptor<CircleInvite> captor = ArgumentCaptor.forClass(CircleInvite.class);
            verify(circleInviteRepository).save(captor.capture());

            assertEquals(expiresAt, captor.getValue().getExpiresAt());
        }

        @Test
        @DisplayName("should throw when user is not member of circle")
        void shouldThrowWhenUserIsNotMember() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> createInviteService.execute(userId, circleId, validInput));

            assertTrue(ex.getMessage().toLowerCase().contains("not a member"));
            verify(circleInviteRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when user is not admin of circle")
        void shouldThrowWhenUserIsNotAdmin() {
            CircleMember regularMember = CircleMember.createMember(circleId, userId);
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(regularMember));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> createInviteService.execute(userId, circleId, validInput));

            assertTrue(ex.getMessage().toLowerCase().contains("admin"));
            verify(circleInviteRepository, never()).save(any());
        }

        @Test
        @DisplayName("should validate member limit before creating invite")
        void shouldValidateMemberLimit() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(adminMember));
            when(circleMemberRepository.countByCircleId(circleId)).thenReturn(2L);
            when(circleInviteRepository.save(any(CircleInvite.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(CircleInvite.class), eq(InviteOutputDto.class)))
                    .thenReturn(InviteOutputDto.builder().build());

            createInviteService.execute(userId, circleId, validInput);

            verify(circleMembershipService).validateMemberLimit(2L);
        }

        @Test
        @DisplayName("should throw when member limit is reached")
        void shouldThrowWhenMemberLimitReached() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(adminMember));
            when(circleMemberRepository.countByCircleId(circleId)).thenReturn(5L);
            doThrow(new IllegalStateException("Circle has reached the maximum number of members (5)"))
                    .when(circleMembershipService).validateMemberLimit(5L);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> createInviteService.execute(userId, circleId, validInput));

            assertTrue(ex.getMessage().contains("maximum"));
            verify(circleInviteRepository, never()).save(any());
        }

        @Test
        @DisplayName("should increment metrics counter on successful invite")
        void shouldIncrementMetricsCounter() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(adminMember));
            when(circleMemberRepository.countByCircleId(circleId)).thenReturn(2L);
            when(circleInviteRepository.save(any(CircleInvite.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(CircleInvite.class), eq(InviteOutputDto.class)))
                    .thenReturn(InviteOutputDto.builder().build());

            createInviteService.execute(userId, circleId, validInput);

            verify(meterRegistry).counter("circles.invites.created");
            verify(counter).increment();
        }
    }
}
