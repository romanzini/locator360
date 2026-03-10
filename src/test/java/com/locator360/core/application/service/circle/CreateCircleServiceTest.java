package com.locator360.core.application.service.circle;

import com.locator360.core.domain.circle.*;
import com.locator360.core.port.in.dto.input.CreateCircleInputDto;
import com.locator360.core.port.in.dto.output.CircleOutputDto;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.CircleRepository;
import com.locator360.core.port.out.CircleSettingsRepository;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCircleServiceTest {

    @Mock
    private CircleRepository circleRepository;

    @Mock
    private CircleMemberRepository circleMemberRepository;

    @Mock
    private CircleSettingsRepository circleSettingsRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private CreateCircleService createCircleService;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        private CreateCircleInputDto validInput;
        private UUID userId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            validInput = new CreateCircleInputDto(
                    "Família Silva",
                    "Grupo da família",
                    "http://photo.jpg",
                    "#4CAF50",
                    "INVITE_ONLY");
            lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        }

        @Test
        @DisplayName("should create circle successfully")
        void shouldCreateCircleSuccessfully() {
            when(circleRepository.save(any(Circle.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.save(any(CircleMember.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleSettingsRepository.save(any(CircleSettings.class))).thenAnswer(inv -> inv.getArgument(0));

            CircleOutputDto mappedOutput = CircleOutputDto.builder()
                    .id(UUID.randomUUID())
                    .name("Família Silva")
                    .privacyLevel("INVITE_ONLY")
                    .build();
            when(modelMapper.map(any(Circle.class), eq(CircleOutputDto.class)))
                    .thenReturn(mappedOutput);

            CircleOutputDto result = createCircleService.execute(userId, validInput);

            assertNotNull(result);
            assertEquals("Família Silva", result.getName());
            assertEquals("INVITE_ONLY", result.getPrivacyLevel());
            assertEquals("ADMIN", result.getRole());
        }

        @Test
        @DisplayName("should save Circle via repository")
        void shouldSaveCircle() {
            when(circleRepository.save(any(Circle.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.save(any(CircleMember.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleSettingsRepository.save(any(CircleSettings.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Circle.class), eq(CircleOutputDto.class)))
                    .thenReturn(CircleOutputDto.builder().build());

            createCircleService.execute(userId, validInput);

            ArgumentCaptor<Circle> captor = ArgumentCaptor.forClass(Circle.class);
            verify(circleRepository).save(captor.capture());

            Circle savedCircle = captor.getValue();
            assertEquals("Família Silva", savedCircle.getName());
            assertEquals("Grupo da família", savedCircle.getDescription());
            assertEquals("http://photo.jpg", savedCircle.getPhotoUrl());
            assertEquals("#4CAF50", savedCircle.getColorHex());
            assertEquals(PrivacyLevel.INVITE_ONLY, savedCircle.getPrivacyLevel());
            assertEquals(userId, savedCircle.getCreatedByUserId());
        }

        @Test
        @DisplayName("should add creator as ADMIN member")
        void shouldAddCreatorAsAdmin() {
            when(circleRepository.save(any(Circle.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.save(any(CircleMember.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleSettingsRepository.save(any(CircleSettings.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Circle.class), eq(CircleOutputDto.class)))
                    .thenReturn(CircleOutputDto.builder().build());

            createCircleService.execute(userId, validInput);

            ArgumentCaptor<CircleMember> captor = ArgumentCaptor.forClass(CircleMember.class);
            verify(circleMemberRepository).save(captor.capture());

            CircleMember savedMember = captor.getValue();
            assertEquals(userId, savedMember.getUserId());
            assertEquals(CircleRole.ADMIN, savedMember.getRole());
            assertEquals(MemberStatus.ACTIVE, savedMember.getStatus());
            assertNotNull(savedMember.getJoinedAt());
        }

        @Test
        @DisplayName("should create default circle settings")
        void shouldCreateDefaultCircleSettings() {
            when(circleRepository.save(any(Circle.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.save(any(CircleMember.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleSettingsRepository.save(any(CircleSettings.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Circle.class), eq(CircleOutputDto.class)))
                    .thenReturn(CircleOutputDto.builder().build());

            createCircleService.execute(userId, validInput);

            ArgumentCaptor<CircleSettings> captor = ArgumentCaptor.forClass(CircleSettings.class);
            verify(circleSettingsRepository).save(captor.capture());

            CircleSettings savedSettings = captor.getValue();
            assertEquals(DrivingAlertLevel.MEDIUM, savedSettings.getDrivingAlertLevel());
            assertTrue(savedSettings.isAllowMemberChat());
            assertTrue(savedSettings.isAllowMemberSos());
        }

        @Test
        @DisplayName("should link circle member to saved circle ID")
        void shouldLinkMemberToCircle() {
            when(circleRepository.save(any(Circle.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.save(any(CircleMember.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleSettingsRepository.save(any(CircleSettings.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Circle.class), eq(CircleOutputDto.class)))
                    .thenReturn(CircleOutputDto.builder().build());

            createCircleService.execute(userId, validInput);

            ArgumentCaptor<Circle> circleCaptor = ArgumentCaptor.forClass(Circle.class);
            verify(circleRepository).save(circleCaptor.capture());
            UUID savedCircleId = circleCaptor.getValue().getId();

            ArgumentCaptor<CircleMember> memberCaptor = ArgumentCaptor.forClass(CircleMember.class);
            verify(circleMemberRepository).save(memberCaptor.capture());
            assertEquals(savedCircleId, memberCaptor.getValue().getCircleId());
        }

        @Test
        @DisplayName("should link circle settings to saved circle ID")
        void shouldLinkSettingsToCircle() {
            when(circleRepository.save(any(Circle.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.save(any(CircleMember.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleSettingsRepository.save(any(CircleSettings.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Circle.class), eq(CircleOutputDto.class)))
                    .thenReturn(CircleOutputDto.builder().build());

            createCircleService.execute(userId, validInput);

            ArgumentCaptor<Circle> circleCaptor = ArgumentCaptor.forClass(Circle.class);
            verify(circleRepository).save(circleCaptor.capture());
            UUID savedCircleId = circleCaptor.getValue().getId();

            ArgumentCaptor<CircleSettings> settingsCaptor = ArgumentCaptor.forClass(CircleSettings.class);
            verify(circleSettingsRepository).save(settingsCaptor.capture());
            assertEquals(savedCircleId, settingsCaptor.getValue().getCircleId());
        }

        @Test
        @DisplayName("should default to OPEN_WITH_CODE when privacy level is null")
        void shouldDefaultPrivacyLevel() {
            CreateCircleInputDto inputWithoutPrivacy = new CreateCircleInputDto(
                    "Família", null, null, null, null);

            when(circleRepository.save(any(Circle.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleMemberRepository.save(any(CircleMember.class))).thenAnswer(inv -> inv.getArgument(0));
            when(circleSettingsRepository.save(any(CircleSettings.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Circle.class), eq(CircleOutputDto.class)))
                    .thenReturn(CircleOutputDto.builder().build());

            createCircleService.execute(userId, inputWithoutPrivacy);

            ArgumentCaptor<Circle> captor = ArgumentCaptor.forClass(Circle.class);
            verify(circleRepository).save(captor.capture());
            assertEquals(PrivacyLevel.OPEN_WITH_CODE, captor.getValue().getPrivacyLevel());
        }
    }
}
