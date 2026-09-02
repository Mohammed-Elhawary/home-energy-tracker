package com.micro.home_energy_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserDTo {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String address;
    private Boolean alerting;
    private Double energyAlertingThreshold;
}
