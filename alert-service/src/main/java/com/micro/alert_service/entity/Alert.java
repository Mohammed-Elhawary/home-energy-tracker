package com.micro.alert_service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor 
@AllArgsConstructor
@Builder
@Table (name = "alert")
public class Alert{
    
    @Id
    @GeneratedValue (strategy =GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDateTime created_at;

    private boolean sent;

}