package com.micro.usage_service.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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
import com.influxdb.client.QueryApi;
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
import com.micro.usage_service.dto.UsageDto;
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

    public UsageService(InfluxDBClient influxDbClient, DeviceClient deviceClient, UserClient userClient,
            KafkaTemplate<String, AlertingEvent> kafkaTemplate) {
        this.influxDbClient = influxDbClient;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent energyUsageEvent) {
        Point point = Point.measurement("energy_usage").addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed", energyUsageEvent.energyUsage())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);

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
                    deviceEnergies.add(DeviceEnergy.builder().deviceId(Long.valueOf(deviceIdStr))
                            .energyConsumed(energyConsumed).build());
                }
            }
        }

        if (deviceEnergies.isEmpty()) {
            return;
        }

        log.info("Aggregated {} device energy records over the past hour", deviceEnergies.size());

        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            try {
                DeviceDto deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());
                if (deviceResponse != null) {
                    deviceEnergy.setUserId(deviceResponse.getUserId());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch Device for ID {}: {}", deviceEnergy.getDeviceId(), e.getMessage());
            }
        }

        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap = deviceEnergies.stream().filter(d -> d.getUserId() != null)
                .collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        userDeviceEnergyMap.forEach((userId, devices) -> {
            try {
                UserDTo userDto = userClient.getUserById(userId);

                if (userDto == null || userDto.getId() == null || !Boolean.TRUE.equals(userDto.getAlerting())) {
                    return;
                }

                double totalConsumption = devices.stream().mapToDouble(DeviceEnergy::getEnergyConsumed).sum();

                Double threshold = userDto.getEnergyAlertingThreshold();

                if (threshold != null && totalConsumption > threshold) {
                    log.info("ALERT: User ID {} exceeded energy threshold! Total: {}, Threshold: {}", userId,
                            totalConsumption, threshold);

                    AlertingEvent alertingEvent = AlertingEvent.builder().userId(userId)
                            .energyConsumed(totalConsumption).email(userDto.getEmail()).threshold(threshold)
                            .message("Energy Consumption threshold exceeded").build();

                    kafkaTemplate.send("energy-alerts", alertingEvent);
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate threshold for User ID {}: {}", userId, e.getMessage());
            }
        });
    }

    public UsageDto getXDayUsageForUser(Long userId, int days) {
        log.info("Getting usage for userId {} over past {} days ", userId, days);

        List<DeviceDto> rawDevices = deviceClient.getAllDeviceForUser(userId);

        final List<DeviceDto> devices = rawDevices != null ? rawDevices : Collections.emptyList();

        if (devices.isEmpty()) {
            return UsageDto.builder().userId(userId).device(Collections.emptyList()).build();
        }

        List<String> deviecIdStrings = devices.stream()
                .map(DeviceDto::getDeviceId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();

        if (deviecIdStrings.isEmpty()) {
            return UsageDto.builder().userId(userId).device(devices).build();
        }

        final Instant now = Instant.now();
        final Instant start = now.minusSeconds((long) days * 24 * 3600);

        String devicesFilter = deviecIdStrings.stream()
                .map(id -> String.format("r[\"deviceId\"] == \"%s\"", id))
                .collect(Collectors.joining(" or "));

        String fluxQuery = String.format("""
                from(bucket: "%s")
                |> range(start: time(v: "%s"), stop: time(v: "%s"))
                |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                |> filter(fn: (r) => r["_field"] == "energyConsumed")
                |> filter(fn: (r) => %s)
                |> group(columns: ["deviceId"])
                |> sum(column: "_value")
                """, influxBucket, start.toString(), now.toString(), devicesFilter);

        final Map<Long, Double> aggregatedMap = new HashMap<>();

        try {
            QueryApi queryApi = influxDbClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object deviceIdObj = record.getValueByKey("deviceId");
                    String deviceIdStr = deviceIdObj == null ? null : deviceIdObj.toString();
                    if (deviceIdStr == null) {
                        continue;
                    }

                    Double energyConsumed = record.getValueByKey("_value") instanceof Number
                            ? ((Number) record.getValueByKey("_value")).doubleValue()
                            : 0.0;

                    try {
                        Long deviceId = Long.valueOf(deviceIdStr);
                        aggregatedMap.put(deviceId, aggregatedMap.getOrDefault(deviceId, 0.0) + energyConsumed);
                    } catch (NumberFormatException nfe) {
                        log.warn("Failed to parse deviceId from flux record: {}", deviceIdStr);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to query InfluxDB for user {} usage over {} days: {}", userId, days, e.getMessage());
        }

        final List<DeviceDto> resultDevices = devices.stream()
                .filter(Objects::nonNull)
                .map(d -> {
                    Double energy = aggregatedMap.getOrDefault(d.getDeviceId(), 0.0);
                    return DeviceDto.builder()
                            .deviceId(d.getDeviceId())
                            .deviceName(d.getDeviceName())
                            .deviceType(d.getDeviceType())
                            .location(d.getLocation())
                            .userId(d.getUserId())
                            .energyConsumer(energy)
                            .build();
                })
                .toList();

        return UsageDto.builder().userId(userId).device(resultDevices).build();
    }
}
