package com.locator360.core.application.service.user;

import com.locator360.core.domain.user.DistanceUnit;
import com.locator360.core.domain.user.User;
import com.locator360.core.domain.user.UserStatus;
import com.locator360.core.port.in.dto.output.UserProfileOutputDto;
import com.locator360.core.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserProfileServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ModelMapper modelMapper;

  @InjectMocks
  private GetUserProfileService getUserProfileService;

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
  }

  @Nested
  @DisplayName("execute")
  class ExecuteTests {

    @Test
    @DisplayName("should return user profile successfully")
    void shouldReturnUserProfile() {
      when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

      UserProfileOutputDto expectedOutput = UserProfileOutputDto.builder()
          .id(userId)
          .email("maria@example.com")
          .phoneNumber("+5511999999999")
          .fullName("Maria Oliveira")
          .firstName("Maria")
          .lastName("Oliveira")
          .preferredLanguage("pt-BR")
          .timezone("America/Sao_Paulo")
          .distanceUnit("KM")
          .status("ACTIVE")
          .build();

      when(modelMapper.map(any(User.class), eq(UserProfileOutputDto.class)))
          .thenReturn(expectedOutput);

      UserProfileOutputDto result = getUserProfileService.execute(userId);

      assertNotNull(result);
      assertEquals(userId, result.getId());
      assertEquals("Maria Oliveira", result.getFullName());
      verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("should throw when user not found")
    void shouldThrowWhenUserNotFound() {
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> getUserProfileService.execute(userId));

      assertTrue(ex.getMessage().contains("User not found"));
    }
  }
}
