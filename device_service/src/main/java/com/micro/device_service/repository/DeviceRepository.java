package com.micro.device_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.micro.device_service.entity.Devices;

@Repository
public interface DeviceRepository extends JpaRepository<Devices, Long> {

    List<Devices> findAllDevicesByUserId(Long userId);

}