package com.micro.insight_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeviceDto(

    @JsonProperty("id") Long id,
    @JsonProperty("deviceName") String name,
    @JsonProperty("deviceType") String type,
    String location,
    @JsonProperty("energyConsumed") double energyConsuming

) {

}
