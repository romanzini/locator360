package com.locator360.core.application.service.user;

import com.locator360.core.port.in.dto.output.DeviceOutputDto;
import com.locator360.core.port.in.user.ListUserDevicesUseCase;
import com.locator360.core.port.out.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ListUserDevicesService implements ListUserDevicesUseCase {

    private final DeviceRepository deviceRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<DeviceOutputDto> execute(UUID userId) {
        log.debug("Listing devices for user: {}", userId);

        List<DeviceOutputDto> devices = deviceRepository.findByUserId(userId).stream()
                .map(device -> modelMapper.map(device, DeviceOutputDto.class))
                .collect(Collectors.toList());

        log.info("Found {} devices for user: {}", devices.size(), userId);
        return devices;
    }
}
