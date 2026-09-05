package org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.sender;

import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.NotificationMessage;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.SendResult;

/**
 * Strategy for sending through one channel.
 */
public interface NotificationSender {
  Channel channel();

  SendResult send(NotificationMessage message);
}
