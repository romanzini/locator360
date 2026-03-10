package com.locator360.core.application.service.user;

import com.locator360.core.domain.user.DistanceUnit;
import com.locator360.core.domain.user.User;
import com.locator360.core.domain.user.UserStatus;
import com.locator360.core.port.in.dto.input.UpdateUserProfileInputDto;
import com.locator360.core.port.in.dto.output.UserProfileOutputDto;
import com.locator360.core.port.out.UserRepository;
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
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ModelMapper modelMapper;

  @Mock
  private MeterRegistry meterRegistry;

  @Mock
  private Counter counter;

  @InjectMocks
  private UpdateUserProfileService updateUserProfileService;

  private UUID userId;
  private User existingUser;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    existingUser = User.restore(userId, "maria@example.com", "+5511999999999",
        "Maria Oliveira", "Maria", "Oliveira",
        null, null, null,
        "pt-BR", "America/Sao_Paulo", DistanceUnit.KM,
        UserStatus.ACTIVE, Instant.now(), Instant.now());
    lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
  }

  @Nested
  @DisplayName("execute")
  class ExecuteTests {

    @Test
    @DisplayName("should update all profile fields successfully")
    void shouldUpdateAllProfileFields() {
      UpdateUserProfileInputDto input = new UpdateUserProfileInputDto(
          "Ana Paula Santos", "Ana Paula", "Santos",
          LocalDate.of(1995, 3, 20), "F", "http://new-photo.jpg",
          "en-US", "America/New_York", "MILES");

      when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
      when(userRepository.save(any(User.class))).thenReturn(existingUser);

      UserProfileOutputDto expectedOutput = UserProfileOutputDto.builder()
          .id(userId)
          .fullName("Ana Paula Santos")
          .build();
      when(modelMapper.map(any(User.class), eq(UserProfileOutputDto.class)))
          .thenReturn(expectedOutput);

      UserProfileOutputDto result = updateUserProfileService.execute(userId, input);

      assertNotNull(result);

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      User savedUser = userCaptor.getValue();
      assertEquals("Ana Paula Santos", savedUser.getFullName());
      assertEquals("Ana Paula", savedUser.getFirstName());
      assertEquals("Santos", savedUser.getLastName());
      assertEquals(LocalDate.of(1995, 3, 20), savedUser.getBirthDate());
      assertEquals("F", savedUser.getGender());
      assertEquals("http://new-photo.jpg", savedUser.getProfilePhotoUrl());
      assertEquals("en-US", savedUser.getPreferredLanguage());
      assertEquals("America/New_York", savedUser.getTimezone());
      assertEquals(DistanceUnit.MILES, savedUser.getDistanceUnit());
    }

    @Test
    @DisplayName("should preserve fields when input has null values")
    void shouldPreserveFieldsWhenInputHasNulls() {
      UpdateUserProfileInputDto input = new UpdateUserProfileInputDto(
          null, null, null, null, null, null, null, null, null);

      when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
      when(userRepository.save(any(User.class))).thenReturn(existingUser);

      UserProfileOutputDto expectedOutput = UserProfileOutputDto.builder()
          .id(userId)
          .fullName("Maria Oliveira")
          .build();
      when(modelMapper.map(any(User.class), eq(UserProfileOutputDto.class)))
          .thenReturn(expectedOutput);

      updateUserProfileService.execute(userId, input);

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      User savedUser = userCaptor.getValue();
      assertEquals("Maria Oliveira", savedUser.getFullName());
      assertEquals("Maria", savedUser.getFirstName());
      assertEquals("pt-BR", savedUser.getPreferredLanguage());
      assertEquals(DistanceUnit.KM, savedUser.getDistanceUnit());
    }

    @Test
    @DisplayName("should throw when user not found")
    void shouldThrowWhenUserNotFound() {
      UpdateUserProfileInputDto input = new UpdateUserProfileInputDto();
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> updateUserProfileService.execute(userId, input));

      assertTrue(ex.getMessage().contains("User not found"));
      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should update only fullName and recalculate names")
    void shouldUpdateOnlyFullName() {
      UpdateUserProfileInputDto input = new UpdateUserProfileInputDto();
      input.setFullName("João Carlos da Silva");

      when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
      when(userRepository.save(any(User.class))).thenReturn(existingUser);
      when(modelMapper.map(any(User.class), eq(UserProfileOutputDto.class)))
          .thenReturn(UserProfileOutputDto.builder().id(userId).build());

      updateUserProfileService.execute(userId, input);

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      User savedUser = userCaptor.getValue();
      assertEquals("João Carlos da Silva", savedUser.getFullName());
      assertEquals("João", savedUser.getFirstName());
      assertEquals("Carlos da Silva", savedUser.getLastName());
    }

    @Test
    @DisplayName("should increment metric on successful update")
    void shouldIncrementMetricOnSuccess() {
      UpdateUserProfileInputDto input = new UpdateUserProfileInputDto();
      input.setPreferredLanguage("en-US");

      when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
      when(userRepository.save(any(User.class))).thenReturn(existingUser);
      when(modelMapper.map(any(User.class), eq(UserProfileOutputDto.class)))
          .thenReturn(UserProfileOutputDto.builder().id(userId).build());

      updateUserProfileService.execute(userId, input);

      verify(meterRegistry).counter("users.profile.updated");
      verify(counter).increment();
    }
  }
}
