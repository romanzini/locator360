package com.locator360.api.rest.user;

import com.locator360.core.port.in.dto.input.UpdateUserProfileInputDto;
import com.locator360.core.port.in.dto.output.UserProfileOutputDto;
import com.locator360.core.port.in.user.GetUserProfileUseCase;
import com.locator360.core.port.in.user.UpdateUserProfileUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserControllerApi {

  private final GetUserProfileUseCase getUserProfileUseCase;
  private final UpdateUserProfileUseCase updateUserProfileUseCase;

  @Override
  public ResponseEntity<UserProfileOutputDto> getProfile() {
    UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.debug("Received get profile request for user: {}", userId);
    UserProfileOutputDto output = getUserProfileUseCase.execute(userId);
    log.info("Profile retrieved for user: {}", userId);
    return ResponseEntity.ok(output);
  }

  @Override
  public ResponseEntity<UserProfileOutputDto> updateProfile(
      @Valid @RequestBody UpdateUserProfileInputDto input) {
    UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.debug("Received update profile request for user: {}", userId);
    UserProfileOutputDto output = updateUserProfileUseCase.execute(userId, input);
    log.info("Profile updated for user: {}", userId);
    return ResponseEntity.ok(output);
  }
}
