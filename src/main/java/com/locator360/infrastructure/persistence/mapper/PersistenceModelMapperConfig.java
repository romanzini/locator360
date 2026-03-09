package com.locator360.infrastructure.persistence.mapper;

import com.locator360.core.domain.user.AuthIdentity;
import com.locator360.core.domain.user.AuthProvider;
import com.locator360.core.domain.user.DistanceUnit;
import com.locator360.core.domain.user.TokenType;
import com.locator360.core.domain.user.User;
import com.locator360.core.domain.user.UserStatus;
import com.locator360.core.domain.user.VerificationToken;
import com.locator360.infrastructure.persistence.postgresql.entity.AuthIdentityJpaEntity;
import com.locator360.infrastructure.persistence.postgresql.entity.UserJpaEntity;
import com.locator360.infrastructure.persistence.postgresql.entity.VerificationTokenJpaEntity;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceModelMapperConfig {

  @Autowired
  public void configureMappings(ModelMapper modelMapper) {
    configureUserMappings(modelMapper);
    configureAuthIdentityMappings(modelMapper);
    configureVerificationTokenMappings(modelMapper);
  }

  private void configureUserMappings(ModelMapper modelMapper) {
    // User → UserJpaEntity
    modelMapper.createTypeMap(User.class, UserJpaEntity.class)
        .addMappings(mapper -> {
          mapper.using((Converter<DistanceUnit, String>) ctx -> ctx.getSource() != null ? ctx.getSource().name() : null)
              .map(User::getDistanceUnit, UserJpaEntity::setDistanceUnit);
          mapper.using((Converter<UserStatus, String>) ctx -> ctx.getSource() != null ? ctx.getSource().name() : null)
              .map(User::getStatus, UserJpaEntity::setStatus);
        });

    // UserJpaEntity → User
    modelMapper.createTypeMap(UserJpaEntity.class, User.class)
        .setConverter(ctx -> {
          UserJpaEntity src = ctx.getSource();
          return User.restore(
              src.getId(), src.getEmail(), src.getPhoneNumber(),
              src.getFullName(), src.getFirstName(), src.getLastName(),
              src.getBirthDate(), src.getGender(), src.getProfilePhotoUrl(),
              src.getPreferredLanguage(), src.getTimezone(),
              src.getDistanceUnit() != null ? DistanceUnit.valueOf(src.getDistanceUnit()) : null,
              src.getStatus() != null ? UserStatus.valueOf(src.getStatus()) : null,
              src.getCreatedAt(), src.getUpdatedAt());
        });
  }

  private void configureAuthIdentityMappings(ModelMapper modelMapper) {
    // AuthIdentity → AuthIdentityJpaEntity
    modelMapper.createTypeMap(AuthIdentity.class, AuthIdentityJpaEntity.class)
        .addMappings(mapper -> mapper
            .using((Converter<AuthProvider, String>) ctx -> ctx.getSource() != null ? ctx.getSource().name() : null)
            .map(AuthIdentity::getProvider, AuthIdentityJpaEntity::setProvider));

    // AuthIdentityJpaEntity → AuthIdentity
    modelMapper.createTypeMap(AuthIdentityJpaEntity.class, AuthIdentity.class)
        .setConverter(ctx -> {
          AuthIdentityJpaEntity src = ctx.getSource();
          return AuthIdentity.restore(
              src.getId(), src.getUserId(),
              src.getProvider() != null ? AuthProvider.valueOf(src.getProvider()) : null,
              src.getProviderUserId(), src.getEmail(), src.getPhoneNumber(),
              src.getPasswordHash(), src.isVerified(), src.getLastLoginAt(),
              src.getCreatedAt(), src.getUpdatedAt());
        });
  }

  private void configureVerificationTokenMappings(ModelMapper modelMapper) {
    // VerificationToken → VerificationTokenJpaEntity
    modelMapper.createTypeMap(VerificationToken.class, VerificationTokenJpaEntity.class)
        .addMappings(mapper -> mapper
            .using((Converter<TokenType, String>) ctx -> ctx.getSource() != null ? ctx.getSource().name() : null)
            .map(VerificationToken::getType, VerificationTokenJpaEntity::setType));

    // VerificationTokenJpaEntity → VerificationToken
    modelMapper.createTypeMap(VerificationTokenJpaEntity.class, VerificationToken.class)
        .setConverter(ctx -> {
          VerificationTokenJpaEntity src = ctx.getSource();
          return VerificationToken.restore(
              src.getId(), src.getUserId(),
              src.getType() != null ? TokenType.valueOf(src.getType()) : null,
              src.getToken(), src.getExpiresAt(),
              src.getUsedAt(), src.getCreatedAt());
        });
  }
}
