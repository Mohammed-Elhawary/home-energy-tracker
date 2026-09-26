package com.micro.usage_service.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record UsageDto(List<DeviceDto> device, double energyConsumed, Long userId) {

}
