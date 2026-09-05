package org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.client;

import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.SendResult;

/**
 * Adapter boundary for an SMS vendor such as Twilio.
 */
public interface SmsProviderClient {
  SendResult sendSms(String phoneNumber, String body);
}
