package com.micro.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlertingEvent {

    Long userId;
    String message;
    double threshold;
    double energyConsumed;
    String email;

}
