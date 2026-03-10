package com.locator360.core.application.config;

import com.locator360.core.domain.circle.Circle;
import com.locator360.core.domain.user.User;
import com.locator360.core.port.in.dto.output.CircleOutputDto;
import com.locator360.core.port.in.dto.output.RegisterUserOutputDto;
import com.locator360.core.port.in.dto.output.UserProfileOutputDto;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

  @Bean
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    configureOutputDtoMappings(modelMapper);
    return modelMapper;
  }

  private void configureOutputDtoMappings(ModelMapper modelMapper) {
    modelMapper.createTypeMap(User.class, RegisterUserOutputDto.class)
        .setConverter(ctx -> {
          User src = ctx.getSource();
          return RegisterUserOutputDto.builder()
              .id(src.getId())
              .email(src.getEmail())
              .phoneNumber(src.getPhoneNumber())
              .fullName(src.getFullName())
              .firstName(src.getFirstName())
              .lastName(src.getLastName())
              .preferredLanguage(src.getPreferredLanguage())
              .timezone(src.getTimezone())
              .distanceUnit(src.getDistanceUnit() != null ? src.getDistanceUnit().name() : null)
              .status(src.getStatus() != null ? src.getStatus().name() : null)
              .createdAt(src.getCreatedAt())
              .updatedAt(src.getUpdatedAt())
              .build();
        });

    modelMapper.createTypeMap(User.class, UserProfileOutputDto.class)
        .setConverter(ctx -> {
          User src = ctx.getSource();
          return UserProfileOutputDto.builder()
              .id(src.getId())
              .email(src.getEmail())
              .phoneNumber(src.getPhoneNumber())
              .fullName(src.getFullName())
              .firstName(src.getFirstName())
              .lastName(src.getLastName())
              .birthDate(src.getBirthDate())
              .gender(src.getGender())
              .profilePhotoUrl(src.getProfilePhotoUrl())
              .preferredLanguage(src.getPreferredLanguage())
              .timezone(src.getTimezone())
              .distanceUnit(src.getDistanceUnit() != null ? src.getDistanceUnit().name() : null)
              .status(src.getStatus() != null ? src.getStatus().name() : null)
              .createdAt(src.getCreatedAt())
              .updatedAt(src.getUpdatedAt())
              .build();
        });

    modelMapper.createTypeMap(Circle.class, CircleOutputDto.class)
        .setConverter(ctx -> {
          Circle src = ctx.getSource();
          return CircleOutputDto.builder()
              .id(src.getId())
              .name(src.getName())
              .description(src.getDescription())
              .photoUrl(src.getPhotoUrl())
              .colorHex(src.getColorHex())
              .privacyLevel(src.getPrivacyLevel() != null ? src.getPrivacyLevel().name() : null)
              .createdByUserId(src.getCreatedByUserId())
              .createdAt(src.getCreatedAt())
              .updatedAt(src.getUpdatedAt())
              .build();
        });
  }
}
