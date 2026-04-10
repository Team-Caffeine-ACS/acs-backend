package com.caffeine.acs_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  public void sendVisitorNotification(
      String to, String visitorName, String arrivalTime, String building, String customMessage) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(to);
      message.setSubject("Visit Pre-Registration Confirmation");
      message.setText(
          String.format(
              "Dear %s,\n\nYou have been pre-registered for a visit.\n\n"
                  + "Expected arrival: %s\nBuilding: %s\n\n"
                  + "%s"
                  + "Please present this email at the reception.\n\nBest regards,\nACS System",
              visitorName,
              arrivalTime,
              building,
              customMessage != null ? customMessage + "\n\n" : ""));
      mailSender.send(message);
      log.info("Notification email sent to {}", to);
    } catch (Exception e) {
      log.error("Failed to send email to {}: {}", to, e.getMessage());
    }
  }

  public void sendCancellationNotification(String to, String visitorName, String customMessage) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(to);
      message.setSubject("Visit Pre-Registration Cancelled");
      message.setText(
          String.format(
              "Dear %s,\n\nYour pre-registration has been cancelled.\n\n%s\n\nBest regards,\nACS System",
              visitorName, customMessage != null ? customMessage : ""));
      mailSender.send(message);
      log.info("Cancellation email sent to {}", to);
    } catch (Exception e) {
      log.error("Failed to send cancellation email to {}: {}", to, e.getMessage());
    }
  }
}
