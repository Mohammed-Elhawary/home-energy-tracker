package com.micro.insight_service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record UsageDto(Long userId, @JsonProperty("device") List<DeviceDto> devices) {

}
