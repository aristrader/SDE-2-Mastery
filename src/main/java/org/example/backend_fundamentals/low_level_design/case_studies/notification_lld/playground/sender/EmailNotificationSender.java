package org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.sender;

import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.client.EmailProviderClient;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.NotificationMessage;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.Recipient;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.SendResult;

public class EmailNotificationSender implements NotificationSender {
  private final EmailProviderClient providerClient;

  public EmailNotificationSender(EmailProviderClient providerClient) {
    this.providerClient = providerClient;
  }

  @Override
  public Channel channel() {
    return Channel.EMAIL;
  }

  @Override
  public SendResult send(NotificationMessage message) {
    Recipient recipient = message.getRecipient();
    if (isBlank(recipient.getEmail())) {
      return SendResult.failure(Channel.EMAIL, "recipient email is missing");
    }
    return providerClient.sendEmail(recipient.getEmail(), message.getSubject(), message.getBody());
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
