package com.locator360.api.rest.user;

import com.locator360.core.port.in.dto.input.UpdateUserProfileInputDto;
import com.locator360.core.port.in.dto.output.DeviceOutputDto;
import com.locator360.core.port.in.dto.output.UserProfileOutputDto;
import com.locator360.core.port.in.user.GetUserProfileUseCase;
import com.locator360.core.port.in.user.ListUserDevicesUseCase;
import com.locator360.core.port.in.user.RevokeDeviceUseCase;
import com.locator360.core.port.in.user.UpdateUserProfileUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserControllerApi {

  private final GetUserProfileUseCase getUserProfileUseCase;
  private final UpdateUserProfileUseCase updateUserProfileUseCase;
  private final ListUserDevicesUseCase listUserDevicesUseCase;
  private final RevokeDeviceUseCase revokeDeviceUseCase;

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

  @Override
  public ResponseEntity<List<DeviceOutputDto>> listDevices() {
    UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.debug("Received list devices request for user: {}", userId);
    List<DeviceOutputDto> devices = listUserDevicesUseCase.execute(userId);
    log.info("Listed {} devices for user: {}", devices.size(), userId);
    return ResponseEntity.ok(devices);
  }

  @Override
  public ResponseEntity<Void> revokeDevice(@PathVariable UUID deviceId) {
    UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.debug("Received revoke device request: {} for user: {}", deviceId, userId);
    revokeDeviceUseCase.execute(userId, deviceId);
    log.info("Device revoked: {} for user: {}", deviceId, userId);
    return ResponseEntity.noContent().build();
  }
}
