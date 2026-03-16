package com.locator360.core.application.service.place;

import com.locator360.core.domain.circle.CircleMember;
import com.locator360.core.domain.circle.CircleRole;
import com.locator360.core.domain.circle.MemberStatus;
import com.locator360.core.domain.place.Place;
import com.locator360.core.domain.place.PlaceAlertPolicy;
import com.locator360.core.domain.place.PlaceType;
import com.locator360.core.port.in.dto.input.CreatePlaceInputDto;
import com.locator360.core.port.in.dto.output.PlaceOutputDto;
import com.locator360.core.port.out.CircleMemberRepository;
import com.locator360.core.port.out.PlaceAlertPolicyRepository;
import com.locator360.core.port.out.PlaceRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private PlaceAlertPolicyRepository placeAlertPolicyRepository;

    @Mock
    private CircleMemberRepository circleMemberRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private CreatePlaceService createPlaceService;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        private UUID userId;
        private UUID circleId;
        private CreatePlaceInputDto validInput;
        private CircleMember activeMember;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            circleId = UUID.randomUUID();
            validInput = new CreatePlaceInputDto("Casa", "HOME", "Rua A, 123",
                    -23.5, -46.6, 100.0);
            activeMember = CircleMember.restore(UUID.randomUUID(), circleId, userId,
                    CircleRole.ADMIN, MemberStatus.ACTIVE, Instant.now(), null,
                    Instant.now(), Instant.now());
            lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        }

        @Test
        @DisplayName("should create place successfully")
        void shouldCreatePlaceSuccessfully() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(activeMember));
            when(placeRepository.save(any(Place.class))).thenAnswer(inv -> inv.getArgument(0));
            when(placeAlertPolicyRepository.save(any(PlaceAlertPolicy.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PlaceOutputDto expectedOutput = PlaceOutputDto.builder()
                    .id(UUID.randomUUID())
                    .circleId(circleId)
                    .name("Casa")
                    .type("HOME")
                    .build();
            when(modelMapper.map(any(Place.class), eq(PlaceOutputDto.class)))
                    .thenReturn(expectedOutput);

            PlaceOutputDto result = createPlaceService.execute(userId, circleId, validInput);

            assertNotNull(result);
            assertEquals("Casa", result.getName());
            verify(placeRepository).save(any(Place.class));
            verify(placeAlertPolicyRepository).save(any(PlaceAlertPolicy.class));
        }

        @Test
        @DisplayName("should save place with correct data")
        void shouldSavePlaceWithCorrectData() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(activeMember));
            when(placeRepository.save(any(Place.class))).thenAnswer(inv -> inv.getArgument(0));
            when(placeAlertPolicyRepository.save(any(PlaceAlertPolicy.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Place.class), eq(PlaceOutputDto.class)))
                    .thenReturn(PlaceOutputDto.builder().build());

            createPlaceService.execute(userId, circleId, validInput);

            ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
            verify(placeRepository).save(captor.capture());

            Place savedPlace = captor.getValue();
            assertEquals(circleId, savedPlace.getCircleId());
            assertEquals("Casa", savedPlace.getName());
            assertEquals(PlaceType.HOME, savedPlace.getType());
            assertEquals("Rua A, 123", savedPlace.getAddressText());
            assertEquals(-23.5, savedPlace.getLatitude());
            assertEquals(-46.6, savedPlace.getLongitude());
            assertEquals(100.0, savedPlace.getRadiusMeters());
            assertTrue(savedPlace.isActive());
            assertEquals(userId, savedPlace.getCreatedByUserId());
        }

        @Test
        @DisplayName("should create default alert policy for the place")
        void shouldCreateDefaultAlertPolicy() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(activeMember));
            when(placeRepository.save(any(Place.class))).thenAnswer(inv -> inv.getArgument(0));
            when(placeAlertPolicyRepository.save(any(PlaceAlertPolicy.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Place.class), eq(PlaceOutputDto.class)))
                    .thenReturn(PlaceOutputDto.builder().build());

            createPlaceService.execute(userId, circleId, validInput);

            ArgumentCaptor<PlaceAlertPolicy> captor = ArgumentCaptor.forClass(PlaceAlertPolicy.class);
            verify(placeAlertPolicyRepository).save(captor.capture());

            PlaceAlertPolicy policy = captor.getValue();
            assertTrue(policy.isAlertOnEnter());
            assertTrue(policy.isAlertOnExit());
        }

        @Test
        @DisplayName("should throw when user is not a member of the circle")
        void shouldThrowWhenUserNotMember() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    createPlaceService.execute(userId, circleId, validInput));

            verify(placeRepository, never()).save(any());
        }

        @Test
        @DisplayName("should increment places.created metric")
        void shouldIncrementMetric() {
            when(circleMemberRepository.findByCircleIdAndUserId(circleId, userId))
                    .thenReturn(Optional.of(activeMember));
            when(placeRepository.save(any(Place.class))).thenAnswer(inv -> inv.getArgument(0));
            when(placeAlertPolicyRepository.save(any(PlaceAlertPolicy.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(Place.class), eq(PlaceOutputDto.class)))
                    .thenReturn(PlaceOutputDto.builder().build());

            createPlaceService.execute(userId, circleId, validInput);

            verify(meterRegistry).counter("places.created");
            verify(counter).increment();
        }
    }
}
