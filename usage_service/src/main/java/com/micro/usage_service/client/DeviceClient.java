package com.micro.usage_service.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.micro.usage_service.dto.DeviceDto;

@Component
public class DeviceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public DeviceClient(@Value("${device.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public DeviceDto getDeviceById(Long deviceId) {
        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl)
                    .pathSegment(String.valueOf(deviceId))
                    .toUriString();

            ResponseEntity<DeviceDto> response = restTemplate.getForEntity(url, DeviceDto.class);
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public List<DeviceDto> getAllDeviceForUser(Long userId) {
        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl)
                    .pathSegment("users", String.valueOf(userId))
                    .toUriString();

            ResponseEntity<DeviceDto[]> response = restTemplate.getForEntity(url, DeviceDto[].class);

            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }
}
