package com.micro.insight_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micro.insight_service.dto.InsightDto;
import com.micro.insight_service.service.InsightService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/insight")
public class InsightController {
    
    private final InsightService insightService;

    InsightController(InsightService insightService) {

        this.insightService = insightService;
    }
    
    @GetMapping("/saving-tips/{userId}")
    public ResponseEntity<InsightDto> getSavingTips(@PathVariable Long userId) {

        InsightDto insight = insightService.getSavingTips(userId);

        return ResponseEntity.ok(insight);
    }

    @GetMapping("/overview/{userId}")
    public ResponseEntity<InsightDto> getOverView(@PathVariable Long userId) {
        
        InsightDto insight = insightService.getOverView(userId);

        return ResponseEntity.ok(insight);

    }
    

}
