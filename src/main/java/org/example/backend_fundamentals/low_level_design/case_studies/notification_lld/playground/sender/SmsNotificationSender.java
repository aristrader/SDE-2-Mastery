package org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.sender;

import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.client.SmsProviderClient;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.NotificationMessage;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.Recipient;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.SendResult;

public class SmsNotificationSender implements NotificationSender {
  private final SmsProviderClient providerClient;

  public SmsNotificationSender(SmsProviderClient providerClient) {
    this.providerClient = providerClient;
  }

  @Override
  public Channel channel() {
    return Channel.SMS;
  }

  @Override
  public SendResult send(NotificationMessage message) {
    Recipient recipient = message.getRecipient();
    if (isBlank(recipient.getPhoneNumber())) {
      return SendResult.failure(Channel.SMS, "recipient phone number is missing");
    }
    return providerClient.sendSms(recipient.getPhoneNumber(), message.getBody());
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
