package com.micro.ingestion_service.service;

import java.time.Instant;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.micro.ingestion_service.dto.EnergyUsageDto;
import com.micro.kafka.event.EnergyUsageEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IngestionService {

    private final KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate;

    public IngestionService(KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void ingestEnergyUsage(EnergyUsageDto energyUsageEvent) {

        EnergyUsageEvent event = EnergyUsageEvent.builder()
                .deviceId(energyUsageEvent.deviceId())
                .energyUsage(energyUsageEvent.energyUsage())
                .timestamp(Instant.now())
                .build();
        
        log.info("Ingesting energy usage data: {}", event);
        kafkaTemplate.sendDefault( event);
    }
}
    