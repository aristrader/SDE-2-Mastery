package org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.client;

import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.SendResult;

public class ConsolePushProviderClient implements PushProviderClient {
  @Override
  public SendResult sendPush(String deviceToken, String title, String body) {
    System.out.println("PUSH token=" + deviceToken + ", title=" + title + ", body=" + body);
    return SendResult.success(Channel.PUSH, "push-001");
  }
}
