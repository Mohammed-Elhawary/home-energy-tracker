package com.micro.usage_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.micro.usage_service.dto.UserDTo;

@Component
public class UserClient {

    private RestTemplate restTemplate;

    private String baseUrl;

    public UserClient(@Value("${user.service.url}") String baseUrl) {

        this.restTemplate = new RestTemplate();

        this.baseUrl = baseUrl;
    }

    public UserDTo getUserById(Long userId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl).path("/{userId}").buildAndExpand(userId).toString();
        ResponseEntity<UserDTo> response = restTemplate.getForEntity(url, UserDTo.class);

        return response.getBody();
    }

}
