package com.locator360.core.port.in.user;

import com.locator360.core.port.in.dto.input.UpdateUserProfileInputDto;
import com.locator360.core.port.in.dto.output.UserProfileOutputDto;

import java.util.UUID;

public interface UpdateUserProfileUseCase {

  UserProfileOutputDto execute(UUID userId, UpdateUserProfileInputDto input);
}
