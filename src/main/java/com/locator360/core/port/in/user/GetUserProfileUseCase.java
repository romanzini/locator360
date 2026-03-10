package com.locator360.core.port.in.user;

import com.locator360.core.port.in.dto.output.UserProfileOutputDto;

import java.util.UUID;

public interface GetUserProfileUseCase {

  UserProfileOutputDto execute(UUID userId);
}
