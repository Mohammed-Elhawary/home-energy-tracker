package com.micro.usage_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDto {

    // ✅ ربط الحقل id بـ deviceId
    @JsonProperty("id")
    private Long deviceId;

    private String deviceName;
    private String deviceType;
    private String location;
    private Long userId;

    // ✅ ربط الحقل energyConsumed بـ energyConsumer
    @JsonProperty("energyConsumed")
    private Double energyConsumer;
}
