package com.micro.device_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.micro.device_service.entity.Devices;
import com.micro.device_service.model.DeviceType;
import com.micro.device_service.repository.DeviceRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class DeviceServiceApplicationTests {

    private final int Users = 10;

    private final int NumOfDevices = 200;

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void createDevice() {
        log.info("Creating device");
        for (int i = 1; i <= NumOfDevices; i++) {
            Devices device = Devices.builder().deviceName("Device " + i).location("location" + ((i % 3) + 1))
                    .deviceType(DeviceType.values()[i % DeviceType.values().length]).userId((long) ((i % Users) + 1))
                    .build();
            deviceRepository.save(device);
        }
        log.info("Created devices have been saved successfully.");
    }
}
