package org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.rendering;

import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.NotificationMessage;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.NotificationRequest;

/**
 * Converts business notification data into channel-ready text.
 */
public interface TemplateRenderer {
  NotificationMessage render(NotificationRequest request, Channel channel);
}
