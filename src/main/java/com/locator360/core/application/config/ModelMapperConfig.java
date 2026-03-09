package com.locator360.core.application.config;

import com.locator360.core.domain.user.User;
import com.locator360.core.port.in.dto.output.RegisterUserOutputDto;
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
  }
}
