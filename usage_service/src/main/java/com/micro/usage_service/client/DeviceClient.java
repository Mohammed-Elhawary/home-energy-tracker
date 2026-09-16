package com.micro.usage_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.micro.usage_service.dto.DeviceDto;

@Component
public class DeviceClient {

    private final RestTemplate restTemplat;

    String baseUrl;

    DeviceClient(@Value("${device.service.url}") String baseUrl) {

        this.restTemplat = new RestTemplate();

        this.baseUrl = baseUrl;
 
    }

    public DeviceDto getDeviceById(Long deviceId) {
        String url = UriComponentsBuilder
        .fromUriString(baseUrl)
        .path("/{deviceId}")
        .buildAndExpand(deviceId)
        .toString();
    ResponseEntity<DeviceDto>response=restTemplat.getForEntity(url,DeviceDto.class);

        return response.getBody();
    }
}