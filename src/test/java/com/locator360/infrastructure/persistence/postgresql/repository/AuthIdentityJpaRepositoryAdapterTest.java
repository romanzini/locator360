package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.user.AuthIdentity;
import com.locator360.core.domain.user.AuthProvider;
import com.locator360.infrastructure.persistence.postgresql.entity.AuthIdentityJpaEntity;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthIdentityJpaRepositoryAdapterTest {

  @Mock
  private SpringDataAuthIdentityRepository springDataAuthIdentityRepository;

  @Mock
  private ModelMapper modelMapper;

  @InjectMocks
  private AuthIdentityJpaRepositoryAdapter authIdentityJpaRepositoryAdapter;

  private AuthIdentity createDomainAuthIdentity() {
    return AuthIdentity.restore(
        UUID.randomUUID(),
        UUID.randomUUID(),
        AuthProvider.PASSWORD,
        null,
        "test@example.com",
        null,
        "hashed_password",
        false,
        null,
        Instant.now(),
        Instant.now());
  }

  private AuthIdentityJpaEntity createJpaEntity() {
    AuthIdentityJpaEntity entity = new AuthIdentityJpaEntity();
    entity.setId(UUID.randomUUID());
    entity.setUserId(UUID.randomUUID());
    entity.setProvider("PASSWORD");
    entity.setEmail("test@example.com");
    entity.setPasswordHash("hashed_password");
    entity.setVerified(false);
    entity.setCreatedAt(Instant.now());
    entity.setUpdatedAt(Instant.now());
    return entity;
  }

  @Nested
  @DisplayName("save")
  class SaveTests {

    @Test
    @DisplayName("should map domain to entity, save, and map back")
    void shouldSaveAndReturnDomain() {
      AuthIdentity domain = createDomainAuthIdentity();
      AuthIdentityJpaEntity jpaEntity = createJpaEntity();
      AuthIdentityJpaEntity savedEntity = createJpaEntity();

      when(modelMapper.map(domain, AuthIdentityJpaEntity.class)).thenReturn(jpaEntity);
      when(springDataAuthIdentityRepository.save(jpaEntity)).thenReturn(savedEntity);
      when(modelMapper.map(savedEntity, AuthIdentity.class)).thenReturn(domain);

      AuthIdentity result = authIdentityJpaRepositoryAdapter.save(domain);

      assertNotNull(result);
      verify(modelMapper).map(domain, AuthIdentityJpaEntity.class);
      verify(springDataAuthIdentityRepository).save(jpaEntity);
      verify(modelMapper).map(savedEntity, AuthIdentity.class);
    }
  }

  @Nested
  @DisplayName("findByUserIdAndProvider")
  class FindByUserIdAndProviderTests {

    @Test
    @DisplayName("should return domain when found")
    void shouldReturnWhenFound() {
      UUID userId = UUID.randomUUID();
      AuthIdentityJpaEntity entity = createJpaEntity();
      AuthIdentity domain = createDomainAuthIdentity();

      when(springDataAuthIdentityRepository.findByUserIdAndProvider(userId, "PASSWORD"))
          .thenReturn(Optional.of(entity));
      when(modelMapper.map(entity, AuthIdentity.class)).thenReturn(domain);

      Optional<AuthIdentity> result = authIdentityJpaRepositoryAdapter
          .findByUserIdAndProvider(userId, AuthProvider.PASSWORD);

      assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("should return empty when not found")
    void shouldReturnEmptyWhenNotFound() {
      UUID userId = UUID.randomUUID();
      when(springDataAuthIdentityRepository.findByUserIdAndProvider(userId, "PASSWORD"))
          .thenReturn(Optional.empty());

      Optional<AuthIdentity> result = authIdentityJpaRepositoryAdapter
          .findByUserIdAndProvider(userId, AuthProvider.PASSWORD);

      assertTrue(result.isEmpty());
    }
  }
}
