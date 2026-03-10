package com.locator360.core.port.out;

public interface NotificationSender {

  void sendEmail(String to, String subject, String body);

  void sendSms(String to, String message);
}