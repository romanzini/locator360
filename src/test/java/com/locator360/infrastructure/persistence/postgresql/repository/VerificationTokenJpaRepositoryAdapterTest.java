package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.user.TokenType;
import com.locator360.core.domain.user.VerificationToken;
import com.locator360.infrastructure.persistence.postgresql.entity.VerificationTokenJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationTokenJpaRepositoryAdapterTest {

  @Mock
  private SpringDataVerificationTokenRepository springDataVerificationTokenRepository;

  @Mock
  private ModelMapper modelMapper;

  @InjectMocks
  private VerificationTokenJpaRepositoryAdapter verificationTokenJpaRepositoryAdapter;

  private VerificationToken createDomainToken() {
    return VerificationToken.restore(
        UUID.randomUUID(),
        UUID.randomUUID(),
        TokenType.EMAIL_VERIFICATION,
        UUID.randomUUID().toString(),
        Instant.now().plus(24, ChronoUnit.HOURS),
        null,
        Instant.now());
  }

  private VerificationTokenJpaEntity createJpaEntity() {
    VerificationTokenJpaEntity entity = new VerificationTokenJpaEntity();
    entity.setId(UUID.randomUUID());
    entity.setUserId(UUID.randomUUID());
    entity.setType("EMAIL_VERIFICATION");
    entity.setToken(UUID.randomUUID().toString());
    entity.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
    entity.setCreatedAt(Instant.now());
    return entity;
  }

  @Nested
  @DisplayName("save")
  class SaveTests {

    @Test
    @DisplayName("should map domain to entity, save, and map back")
    void shouldSaveAndReturnDomain() {
      VerificationToken domain = createDomainToken();
      VerificationTokenJpaEntity jpaEntity = createJpaEntity();
      VerificationTokenJpaEntity savedEntity = createJpaEntity();

      when(modelMapper.map(domain, VerificationTokenJpaEntity.class)).thenReturn(jpaEntity);
      when(springDataVerificationTokenRepository.save(jpaEntity)).thenReturn(savedEntity);
      when(modelMapper.map(savedEntity, VerificationToken.class)).thenReturn(domain);

      VerificationToken result = verificationTokenJpaRepositoryAdapter.save(domain);

      assertNotNull(result);
      verify(modelMapper).map(domain, VerificationTokenJpaEntity.class);
      verify(springDataVerificationTokenRepository).save(jpaEntity);
      verify(modelMapper).map(savedEntity, VerificationToken.class);
    }
  }

  @Nested
  @DisplayName("findByToken")
  class FindByTokenTests {

    @Test
    @DisplayName("should return domain when found by token string")
    void shouldReturnWhenFound() {
      String tokenStr = UUID.randomUUID().toString();
      VerificationTokenJpaEntity entity = createJpaEntity();
      VerificationToken domain = createDomainToken();

      when(springDataVerificationTokenRepository.findByToken(tokenStr))
          .thenReturn(Optional.of(entity));
      when(modelMapper.map(entity, VerificationToken.class)).thenReturn(domain);

      Optional<VerificationToken> result = verificationTokenJpaRepositoryAdapter.findByToken(tokenStr);

      assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("should return empty when not found")
    void shouldReturnEmptyWhenNotFound() {
      String tokenStr = UUID.randomUUID().toString();
      when(springDataVerificationTokenRepository.findByToken(tokenStr))
          .thenReturn(Optional.empty());

      Optional<VerificationToken> result = verificationTokenJpaRepositoryAdapter.findByToken(tokenStr);

      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("findByUserIdAndType")
  class FindByUserIdAndTypeTests {

    @Test
    @DisplayName("should return domain when found by userId and type")
    void shouldReturnWhenFound() {
      UUID userId = UUID.randomUUID();
      VerificationTokenJpaEntity entity = createJpaEntity();
      VerificationToken domain = createDomainToken();

      when(springDataVerificationTokenRepository.findByUserIdAndType(
          userId, "EMAIL_VERIFICATION"))
          .thenReturn(Optional.of(entity));
      when(modelMapper.map(entity, VerificationToken.class)).thenReturn(domain);

      Optional<VerificationToken> result = verificationTokenJpaRepositoryAdapter.findByUserIdAndType(
          userId, TokenType.EMAIL_VERIFICATION);

      assertTrue(result.isPresent());
    }
  }
}
