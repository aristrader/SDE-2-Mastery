package org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.client;

import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.SendResult;

public class ConsoleSmsProviderClient implements SmsProviderClient {
  @Override
  public SendResult sendSms(String phoneNumber, String body) {
    System.out.println("SMS to=" + phoneNumber + ", body=" + body);
    return SendResult.success(Channel.SMS, "sms-001");
  }
}
