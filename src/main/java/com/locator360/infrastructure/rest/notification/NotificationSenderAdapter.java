package com.locator360.infrastructure.rest.notification;

import com.locator360.core.port.out.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationSenderAdapter implements NotificationSender {

  @Override
  public void sendEmail(String to, String subject, String body) {
    log.info("Sending email notification to: {}", to);
    log.debug("Email notification subject: {}", subject);
  }

  @Override
  public void sendSms(String to, String message) {
    log.info("Sending SMS notification to: {}", to);
  }
}