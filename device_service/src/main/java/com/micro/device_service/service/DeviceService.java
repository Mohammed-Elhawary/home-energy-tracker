package com.micro.device_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.micro.device_service.dto.DeviceDto;
import com.micro.device_service.entity.Devices;
import com.micro.device_service.exception.DeviceNotFoundExceptions;
import com.micro.device_service.repository.DeviceRepository;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceDto getDeviceId(Long deviceId) {
        Devices device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundExceptions("Device not found with id: " + deviceId));
        return mapToDto(device);
    }

    public DeviceDto createDevice(DeviceDto input) {

        Devices device = new Devices();
        device.setDeviceName(input.getDeviceName());
        device.setDeviceType(input.getDeviceType());
        device.setLocation(input.getLocation());
        device.setUserId(input.getUserId());

        Devices savedDevice = deviceRepository.save(device);
        return mapToDto(savedDevice);
    }

    public DeviceDto updateDevice(Long deviceId, DeviceDto deviceDto) {
        Devices existingDevice = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundExceptions("Device not found with id: " + deviceId));

        existingDevice.setDeviceName(deviceDto.getDeviceName());
        existingDevice.setDeviceType(deviceDto.getDeviceType());
        existingDevice.setLocation(deviceDto.getLocation());
        existingDevice.setUserId(deviceDto.getUserId());

        Devices updatedDevice = deviceRepository.save(existingDevice);
        return mapToDto(updatedDevice);
    }

    public void deleteDevice(Long deviceId) {
        Devices existingDevice = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundExceptions("Device not found with id: " + deviceId));
        deviceRepository.delete(existingDevice);
    }

    private DeviceDto mapToDto(Devices device) {
        return DeviceDto.builder().id(device.getId()).deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType()).location(device.getLocation()).userId(device.getUserId()).build();
    }

    public List<DeviceDto> getAllDeviceForUser(Long userId) {
        List<Devices> devices = deviceRepository.findAllDevicesByUserId(userId);
        return devices.stream().map(this::mapToDto).toList();
    }

}
