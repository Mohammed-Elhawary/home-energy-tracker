package com.micro.usage_service.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.micro.kafka.event.AlertingEvent;
import com.micro.kafka.event.EnergyUsageEvent;
import com.micro.usage_service.client.DeviceClient;
import com.micro.usage_service.client.UserClient;
import com.micro.usage_service.dto.DeviceDto;
import com.micro.usage_service.dto.UserDTo;
import com.micro.usage_service.model.DeviceEnergy;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UsageService {

    private final InfluxDBClient influxDbClient;
    private final DeviceClient deviceClient;
    private final UserClient userClient;
    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    @Value("${influx.org}")
    private String influxOrg;

    @Value("${influx.bucket}")
    private String influxBucket;

    public UsageService(InfluxDBClient influxDbClient, 
                        DeviceClient deviceClient, 
                        UserClient userClient,
                        KafkaTemplate<String, AlertingEvent> kafkaTemplate) {
        this.influxDbClient = influxDbClient;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent energyUsageEvent) {
        Point point = Point.measurement("energy_usage")
                .addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed", energyUsageEvent.energyUsage())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);

        // Non-blocking asynchronous write batching
        try (WriteApi writeApi = influxDbClient.makeWriteApi()) {
            writeApi.writePoint(influxBucket, influxOrg, point);
        }
    }

    @Scheduled(cron = "0/10 * * * * *")
    public void aggregateDeviceEnergy() {
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minusSeconds(3600);

        String fluxQuery = String.format("""
                from(bucket: "%s")
                |> range(start: time(v: "%s"), stop: time(v: "%s"))
                |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                |> filter(fn: (r) => r["_field"] == "energyConsumed")
                |> group(columns: ["deviceId"])
                |> sum(column: "_value")
                """, influxBucket, oneHourAgo, now);

        List<FluxTable> tables = influxDbClient.getQueryApi().query(fluxQuery, influxOrg);
        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String deviceIdStr = (String) record.getValueByKey("deviceId");
                Object val = record.getValueByKey("_value");
                Double energyConsumed = (val instanceof Number num) ? num.doubleValue() : 0.0;

                if (deviceIdStr != null) {
                    deviceEnergies.add(DeviceEnergy.builder()
                            .deviceId(Long.valueOf(deviceIdStr))
                            .energyConsumed(energyConsumed)
                            .build());
                }
            }
        }

        if (deviceEnergies.isEmpty()) {
            return;
        }

        log.info("Aggregated {} device energy records over the past hour", deviceEnergies.size());

        // Fetch device details and attach user IDs
        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            try {
                DeviceDto deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());
                if (deviceResponse != null) {
                    deviceEnergy.setUserId(deviceResponse.userId());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch Device for ID {}: {}", deviceEnergy.getDeviceId(), e.getMessage());
            }
        }

        // Group valid records by userId
        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap = deviceEnergies.stream()
                .filter(d -> d.getUserId() != null)
                .collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        // Evaluate thresholds per user
        userDeviceEnergyMap.forEach((userId, devices) -> {
            try {
                UserDTo userDto = userClient.getUserById(userId);

                if (userDto == null || userDto.getId() == null || !Boolean.TRUE.equals(userDto.getAlerting())) {
                    return;
                }

                double totalConsumption = devices.stream()
                        .mapToDouble(DeviceEnergy::getEnergyConsumed)
                        .sum();

                Double threshold = userDto.getEnergyAlertingThreshold();

                if (threshold != null && totalConsumption > threshold) {
                    log.info("ALERT: User ID {} exceeded energy threshold! Total: {}, Threshold: {}", 
                            userId, totalConsumption, threshold);

                    AlertingEvent alertingEvent = AlertingEvent.builder()
                            .userId(userId)
                            .energyConsumed(totalConsumption)
                            .email(userDto.getEmail())
                            .threshold(threshold)
                            .message("Energy Consumption threshold exceeded")
                            .build();

                    kafkaTemplate.send("energy-alerts", alertingEvent);
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate threshold for User ID {}: {}", userId, e.getMessage());
            }
        });
    }
}