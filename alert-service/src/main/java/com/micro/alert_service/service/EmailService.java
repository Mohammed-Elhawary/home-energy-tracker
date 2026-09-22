package com.micro.alert_service.service;

import java.time.LocalDateTime;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.micro.alert_service.entity.Alert;
import com.micro.alert_service.repository.AlertRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    private final AlertRepository alertRepository;

    EmailService(JavaMailSender mailSender, AlertRepository alertRepository) {
        this.mailSender = mailSender;
        this.alertRepository = alertRepository;
    }

    public void sendEmail(
            String to,
            String subject,
            String body,
            Long userId) {

        log.info("Sending Email to : {} ,subject :{} ", to, subject);

        // Email sending  logic would go here
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("elhawaryh0@gmail.com");
        message.setSubject(subject);
        message.setText(body);


        try {
            mailSender.send(message);
            final Alert alertSent = Alert.builder()
                    .userId(userId)
                    .sent(true)
                    .created_at(LocalDateTime.now())
                    .build();
            alertRepository.save(alertSent);
        } catch (Exception ex) {
            log.error("Failed Send message to {} ex {}", to, ex);
            final Alert alertSent = Alert.builder()
                    .userId(userId)
                    .sent(true)
                    .created_at(LocalDateTime.now())
                    .build();   
            alertRepository.saveAndFlush(alertSent);
        }

    }

}
