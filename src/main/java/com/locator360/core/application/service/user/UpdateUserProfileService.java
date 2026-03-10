package com.locator360.core.application.service.user;

import com.locator360.core.domain.user.DistanceUnit;
import com.locator360.core.domain.user.User;
import com.locator360.core.port.in.dto.input.UpdateUserProfileInputDto;
import com.locator360.core.port.in.dto.output.UserProfileOutputDto;
import com.locator360.core.port.in.user.UpdateUserProfileUseCase;
import com.locator360.core.port.out.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UpdateUserProfileService implements UpdateUserProfileUseCase {

  private final UserRepository userRepository;
  private final ModelMapper modelMapper;
  private final MeterRegistry meterRegistry;

  @Override
  public UserProfileOutputDto execute(UUID userId, UpdateUserProfileInputDto input) {
    log.debug("Updating profile for user: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    DistanceUnit distanceUnit = input.getDistanceUnit() != null
        ? DistanceUnit.valueOf(input.getDistanceUnit())
        : null;

    user.updateProfile(
        input.getFullName(),
        input.getFirstName(),
        input.getLastName(),
        input.getBirthDate(),
        input.getGender(),
        input.getProfilePhotoUrl(),
        input.getPreferredLanguage(),
        input.getTimezone(),
        distanceUnit);

    User savedUser = userRepository.save(user);

    log.info("Profile updated for user: {}", userId);
    meterRegistry.counter("users.profile.updated").increment();

    return modelMapper.map(savedUser, UserProfileOutputDto.class);
  }
}
