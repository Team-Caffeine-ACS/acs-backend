package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;

  @InjectMocks private EmailService emailService;

  @Test
  void sendVisitorNotification_success_withCustomMessage() {
    // GIVEN
    String to = "visitor@example.com";
    String visitorName = "John Doe";
    String arrivalTime = "2026-05-01 12:00";
    String building = "Main Office";
    String customMessage = "Please bring your ID.";

    // ArgumentCaptor lubab meil kinni püüda objekti, mis meetodile saadeti
    ArgumentCaptor<SimpleMailMessage> messageCaptor =
        ArgumentCaptor.forClass(SimpleMailMessage.class);

    // ACT
    emailService.sendVisitorNotification(to, visitorName, arrivalTime, building, customMessage);

    // ASSERT
    verify(mailSender).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertThat(sentMessage.getTo()).containsExactly(to);
    assertThat(sentMessage.getSubject()).isEqualTo("Visit Pre-Registration Confirmation");
    assertThat(sentMessage.getText()).contains(visitorName);
    assertThat(sentMessage.getText()).contains(customMessage);
  }

  @Test
  void sendVisitorNotification_success_noCustomMessage() {
    // Testime hargnemist, kus customMessage on null
    emailService.sendVisitorNotification("to@ex.com", "Name", "Time", "B", null);

    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendVisitorNotification_exception_isCaughtAndLogged() {
    // Simuleerime olukorda, kus mailSender viskab vea (nt võrgu viga)
    doThrow(new RuntimeException("SMTP Server down"))
        .when(mailSender)
        .send(any(SimpleMailMessage.class));

    // See ei tohiks visata erindit välja, sest koodis on try-catch
    emailService.sendVisitorNotification("to@ex.com", "Name", "Time", "B", "Msg");

    verify(mailSender).send(any(SimpleMailMessage.class));
    // Kui test siia jõuab ilma veata, on catch-plokk kaetud!
  }

  @Test
  void sendCancellationNotification_success() {
    String to = "visitor@example.com";
    ArgumentCaptor<SimpleMailMessage> messageCaptor =
        ArgumentCaptor.forClass(SimpleMailMessage.class);

    emailService.sendCancellationNotification(to, "Jane Doe", "Reason: Holiday");

    verify(mailSender).send(messageCaptor.capture());
    assertThat(messageCaptor.getValue().getSubject()).isEqualTo("Visit Pre-Registration Cancelled");
    assertThat(messageCaptor.getValue().getText()).contains("Holiday");
  }

  @Test
  void sendCancellationNotification_noCustomMessage() {
    // Testime hargnemist, kus cancellation message on null
    emailService.sendCancellationNotification("to@ex.com", "Jane", null);

    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendCancellationNotification_exception_isCaught() {
    doThrow(new RuntimeException("Error")).when(mailSender).send(any(SimpleMailMessage.class));

    emailService.sendCancellationNotification("to@ex.com", "Jane", "Msg");

    verify(mailSender).send(any(SimpleMailMessage.class));
  }
}
