package com.micro.insight_service.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data 
public class InsightDto {
    Long userId;
    String tips;
    double energyConsuming;
}
