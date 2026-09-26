package com.micro.usage_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.micro.usage_service.dto.UsageDto;
import com.micro.usage_service.service.UsageService;

@RestController
@RequestMapping("/api/v1/usage")
public class UsageController {

    private final UsageService usageService;

    UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UsageDto> getUserDeviceUsage(@PathVariable Long userId,
            @RequestParam(defaultValue = "3") int day) {
        UsageDto usage = usageService.getXDayUsageForUser(userId, day);
        return ResponseEntity.ok(usage);
    }

}
