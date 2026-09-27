package com.micro.ingestion_service.simulation;

import java.time.Instant;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.micro.ingestion_service.dto.EnergyUsageDto;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ContinusDataSimulator implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();



    @Value("${simulation.interval-ms}")
    private int requestInterval;
    
    @Value("${simulation.endpoint}")
    private String ingestionEndPoint;

    @Override
    public void run(String... args) throws Exception {
        log.info("ContinueDataSimulation running .....");
    }

            @Scheduled(fixedRateString = "${simulation.interval-ms}")
            public void sendMockData() {
                for (int i = 0; i < requestInterval; i++) {
                    EnergyUsageDto dto = EnergyUsageDto.builder()
                            .deviceId(random.nextLong(1, 6))
                            .energyUsage(Math.round(random.nextDouble(0.0, 2.0) * 100) / 100)
                            .timestamp(Instant.now())
                            .build();
                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);

                        HttpEntity<EnergyUsageDto> request = new HttpEntity<>(dto, headers);
                        restTemplate.postForEntity(ingestionEndPoint, request, void.class);
                        
                        log.info("Mock data sent successfully for device {}", dto);
                        

                    } catch (Exception e) {
                        log.error("Error sending mock data for device {}", e.getMessage());
                    }
                }
            }

}
