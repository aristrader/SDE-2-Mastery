package org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.client;

import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.SendResult;

/**
 * Adapter boundary for an email vendor such as SendGrid or SES.
 */
public interface EmailProviderClient {
  SendResult sendEmail(String to, String subject, String body);
}
