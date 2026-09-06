package org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.client;

import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.SendResult;

/**
 * Adapter boundary for a push vendor such as Firebase Cloud Messaging.
 */
public interface PushProviderClient {
  SendResult sendPush(String deviceToken, String title, String body);
}
