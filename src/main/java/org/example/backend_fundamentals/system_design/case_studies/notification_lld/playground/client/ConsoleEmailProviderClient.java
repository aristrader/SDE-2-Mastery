package org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.client;

import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.SendResult;

public class ConsoleEmailProviderClient implements EmailProviderClient {
  @Override
  public SendResult sendEmail(String to, String subject, String body) {
    System.out.println("EMAIL to=" + to + ", subject=" + subject + ", body=" + body);
    return SendResult.success(Channel.EMAIL, "email-001");
  }
}
