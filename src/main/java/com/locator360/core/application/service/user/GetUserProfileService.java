package com.locator360.core.application.service.user;

import com.locator360.core.domain.user.User;
import com.locator360.core.port.in.dto.output.UserProfileOutputDto;
import com.locator360.core.port.in.user.GetUserProfileUseCase;
import com.locator360.core.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GetUserProfileService implements GetUserProfileUseCase {

  private final UserRepository userRepository;
  private final ModelMapper modelMapper;

  @Override
  public UserProfileOutputDto execute(UUID userId) {
    log.debug("Fetching profile for user: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    log.info("Profile fetched for user: {}", userId);
    return modelMapper.map(user, UserProfileOutputDto.class);
  }
}
