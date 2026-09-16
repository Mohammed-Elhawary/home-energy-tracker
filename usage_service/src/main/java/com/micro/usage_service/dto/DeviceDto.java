package com.micro.usage_service.dto;

import lombok.Builder;

@Builder
public record DeviceDto(Long deviceId, String deviceName, String deviceType, String location, Long userId) {
}