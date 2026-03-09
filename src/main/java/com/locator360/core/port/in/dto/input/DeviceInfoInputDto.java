package com.locator360.core.port.in.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoInputDto {

  private String platform;
  private String deviceModel;
  private String osVersion;
  private String appVersion;
  private String pushToken;
}
