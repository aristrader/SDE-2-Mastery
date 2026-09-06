package org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.sender;

import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.client.PushProviderClient;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.NotificationMessage;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.Recipient;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.SendResult;

public class PushNotificationSender implements NotificationSender {
  private final PushProviderClient providerClient;

  public PushNotificationSender(PushProviderClient providerClient) {
    this.providerClient = providerClient;
  }

  @Override
  public Channel channel() {
    return Channel.PUSH;
  }

  @Override
  public SendResult send(NotificationMessage message) {
    Recipient recipient = message.getRecipient();
    if (isBlank(recipient.getDeviceToken())) {
      return SendResult.failure(Channel.PUSH, "recipient device token is missing");
    }
    return providerClient.sendPush(recipient.getDeviceToken(), message.getTitle(), message.getBody());
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
