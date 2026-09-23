package com.micro.device_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micro.device_service.dto.DeviceDto;
import com.micro.device_service.service.DeviceService;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceDto> getDeviceId(@PathVariable Long deviceId) {
        DeviceDto deviceDto = deviceService.getDeviceId(deviceId);
        return ResponseEntity.ok(deviceDto);
    }

    @PostMapping("/create")
    public ResponseEntity<DeviceDto> createDevice(@RequestBody DeviceDto deviceDto) {
        DeviceDto createdDevice = deviceService.createDevice(deviceDto);
        return ResponseEntity.ok(createdDevice);
    }

    @PutMapping("/update/{deviceId}")
    public ResponseEntity<DeviceDto> updateDevice(@PathVariable Long deviceId, @RequestBody DeviceDto deviceDto) {
        // Implement the update logic in the service layer
        DeviceDto updatedDevice = deviceService.updateDevice(deviceId, deviceDto);
        return ResponseEntity.ok(updatedDevice);
    }

   @GetMapping ("/users/{userId}")
    public ResponseEntity<List<DeviceDto>> getAllDevices(@PathVariable Long userId) {
        // Implement the update logic in the service layer
        List<DeviceDto> devices = deviceService.getAllDeviceForUser(userId);
        return ResponseEntity.ok(devices);
    }

    @DeleteMapping("/delete/{deviceId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }
}
