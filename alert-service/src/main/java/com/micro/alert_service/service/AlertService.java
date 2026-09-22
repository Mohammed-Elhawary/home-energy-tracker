package com.micro.alert_service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.micro.kafka.event.AlertingEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AlertService {

    private final EmailService emailService;

    public AlertService(EmailService emailService) {
        this.emailService = emailService;
    }


    @KafkaListener(topics = "energy-alerts", groupId = "alert-service")
    public void alertService(AlertingEvent alertingEvent) {

        log.info("Received Alert Event : {} ", alertingEvent.getMessage());

        final String subject = "Energy Usage Alert for User " + alertingEvent.getUserId();

        final String message = "Alert : " + alertingEvent.getMessage()
                + "\nThreShold " + alertingEvent.getThreshold()
                + "\nEnergy Consumer " + alertingEvent.getEnergyConsumed();
        emailService.sendEmail(alertingEvent.getEmail(), subject, message, alertingEvent.getUserId());
    }

}
