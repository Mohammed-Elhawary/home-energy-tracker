package com.micro.ingestion_service.simulation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.micro.ingestion_service.dto.EnergyUsageDto;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class parallelDataSimulator implements CommandLineRunner {

    RestTemplate restTemplate = new RestTemplate();


    private final ExecutorService executorService;

    @Value("${simulation.parallel-threads}")
    private int parallelThreads;

    @Value("${simulation.request-per-interval}")
    private int requestPerInterval;


    @Value("${simulation.endpoint}")
    private String ingestionEndPoint;
    public parallelDataSimulator() {
        this.executorService = Executors.newCachedThreadPool();
    }

    private final Random random = new Random();
    @Override
    public void run(String... args) throws Exception {
        log.info("ParallelDataSimulation running .....");
        ((ThreadPoolExecutor) executorService).setCorePoolSize(parallelThreads);
    }

    @Scheduled(fixedRateString="${simulation.interval-ms}")
    public void sendMockData() {
        log.info("Simulating data .....");
        int batchSize = requestPerInterval / parallelThreads;
        int remaining = requestPerInterval % parallelThreads;
        for (int i = 0; i < parallelThreads; i++) {
            int requestsForThread = batchSize * (i<  remaining ? 1 : 0);
            executorService.submit(() -> {
                for (int j = 0; j < requestsForThread; j++) {
                    EnergyUsageDto dto = EnergyUsageDto.builder()
                            .deviceId(random.nextLong(1, 200))
                            .energyUsage(Math.round(random.nextDouble(0.0, 2.0) * 100) / 100)
                            .timestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
                            .build();
                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<EnergyUsageDto> entity = new HttpEntity<>(dto, headers);
                        restTemplate.postForEntity(ingestionEndPoint, entity, Void.class);
                        log.info("send data Success ... ");
                    } catch (Exception e) {
                        log.error("Error sending mock data to the server .....", e);
                    }
                }
            });
        }
    }

    @PreDestroy
    public  void shutdown(){
        executorService.close();
        log.info("parallelDataSimulator Shutdown... ");
        
    }
    
}