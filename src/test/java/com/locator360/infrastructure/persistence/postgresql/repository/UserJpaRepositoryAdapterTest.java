package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.user.DistanceUnit;
import com.locator360.core.domain.user.User;
import com.locator360.core.domain.user.UserStatus;
import com.locator360.infrastructure.persistence.postgresql.entity.UserJpaEntity;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserJpaRepositoryAdapterTest {

  @Mock
  private SpringDataUserRepository springDataUserRepository;

  @Mock
  private ModelMapper modelMapper;

  @InjectMocks
  private UserJpaRepositoryAdapter userJpaRepositoryAdapter;

  private User createDomainUser() {
    return User.restore(
        UUID.randomUUID(),
        "test@example.com",
        "+5511999999999",
        "Test User",
        "Test",
        "User",
        null, null, null,
        "pt-BR",
        "America/Sao_Paulo",
        DistanceUnit.KM,
        UserStatus.PENDING_VERIFICATION,
        Instant.now(),
        Instant.now());
  }

  private UserJpaEntity createJpaEntity() {
    UserJpaEntity entity = new UserJpaEntity();
    entity.setId(UUID.randomUUID());
    entity.setEmail("test@example.com");
    entity.setPhoneNumber("+5511999999999");
    entity.setFullName("Test User");
    entity.setFirstName("Test");
    entity.setLastName("User");
    entity.setPreferredLanguage("pt-BR");
    entity.setTimezone("America/Sao_Paulo");
    entity.setDistanceUnit("KM");
    entity.setStatus("PENDING_VERIFICATION");
    entity.setCreatedAt(Instant.now());
    entity.setUpdatedAt(Instant.now());
    return entity;
  }

  @Nested
  @DisplayName("save")
  class SaveTests {

    @Test
    @DisplayName("should map domain to entity, save, and map back to domain")
    void shouldSaveAndReturnDomain() {
      User domainUser = createDomainUser();
      UserJpaEntity jpaEntity = createJpaEntity();
      UserJpaEntity savedEntity = createJpaEntity();

      when(modelMapper.map(domainUser, UserJpaEntity.class)).thenReturn(jpaEntity);
      when(springDataUserRepository.save(jpaEntity)).thenReturn(savedEntity);
      when(modelMapper.map(savedEntity, User.class)).thenReturn(domainUser);

      User result = userJpaRepositoryAdapter.save(domainUser);

      assertNotNull(result);
      verify(modelMapper).map(domainUser, UserJpaEntity.class);
      verify(springDataUserRepository).save(jpaEntity);
      verify(modelMapper).map(savedEntity, User.class);
    }
  }

  @Nested
  @DisplayName("findById")
  class FindByIdTests {

    @Test
    @DisplayName("should return domain user when found")
    void shouldReturnUserWhenFound() {
      UUID userId = UUID.randomUUID();
      UserJpaEntity entity = createJpaEntity();
      User domainUser = createDomainUser();

      when(springDataUserRepository.findById(userId)).thenReturn(Optional.of(entity));
      when(modelMapper.map(entity, User.class)).thenReturn(domainUser);

      Optional<User> result = userJpaRepositoryAdapter.findById(userId);

      assertTrue(result.isPresent());
      verify(springDataUserRepository).findById(userId);
    }

    @Test
    @DisplayName("should return empty when not found")
    void shouldReturnEmptyWhenNotFound() {
      UUID userId = UUID.randomUUID();
      when(springDataUserRepository.findById(userId)).thenReturn(Optional.empty());

      Optional<User> result = userJpaRepositoryAdapter.findById(userId);

      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("findByEmail")
  class FindByEmailTests {

    @Test
    @DisplayName("should return domain user when found by email")
    void shouldReturnUserWhenFoundByEmail() {
      UserJpaEntity entity = createJpaEntity();
      User domainUser = createDomainUser();

      when(springDataUserRepository.findByEmail("test@example.com"))
          .thenReturn(Optional.of(entity));
      when(modelMapper.map(entity, User.class)).thenReturn(domainUser);

      Optional<User> result = userJpaRepositoryAdapter.findByEmail("test@example.com");

      assertTrue(result.isPresent());
    }
  }

  @Nested
  @DisplayName("existsByEmail")
  class ExistsByEmailTests {

    @Test
    @DisplayName("should return true when email exists")
    void shouldReturnTrueWhenExists() {
      when(springDataUserRepository.existsByEmail("test@example.com")).thenReturn(true);

      assertTrue(userJpaRepositoryAdapter.existsByEmail("test@example.com"));
    }

    @Test
    @DisplayName("should return false when email does not exist")
    void shouldReturnFalseWhenNotExists() {
      when(springDataUserRepository.existsByEmail("test@example.com")).thenReturn(false);

      assertFalse(userJpaRepositoryAdapter.existsByEmail("test@example.com"));
    }
  }

  @Nested
  @DisplayName("existsByPhoneNumber")
  class ExistsByPhoneNumberTests {

    @Test
    @DisplayName("should return true when phone exists")
    void shouldReturnTrueWhenExists() {
      when(springDataUserRepository.existsByPhoneNumber("+5511999999999")).thenReturn(true);

      assertTrue(userJpaRepositoryAdapter.existsByPhoneNumber("+5511999999999"));
    }
  }
}
