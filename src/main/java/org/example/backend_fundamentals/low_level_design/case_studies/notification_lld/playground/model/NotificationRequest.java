package org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Input command for sending one notification over one or more channels.
 */
public class NotificationRequest {
  private final NotificationType type;
  private final Recipient recipient;
  private final Map<String, String> templateData;
  private final List<Channel> channels;

  public NotificationRequest(NotificationType type, Recipient recipient,
      Map<String, String> templateData, List<Channel> channels) {
    if (type == null) {
      throw new IllegalArgumentException("notification type is required");
    }
    if (recipient == null) {
      throw new IllegalArgumentException("recipient is required");
    }
    if (channels == null || channels.isEmpty()) {
      throw new IllegalArgumentException("at least one channel is required");
    }

    this.type = type;
    this.recipient = recipient;
    this.templateData = templateData == null
        ? Collections.<String, String>emptyMap()
        : Collections.unmodifiableMap(templateData);
    this.channels = Collections.unmodifiableList(channels);
  }

  public NotificationType getType() {
    return type;
  }

  public Recipient getRecipient() {
    return recipient;
  }

  public Map<String, String> getTemplateData() {
    return templateData;
  }

  public List<Channel> getChannels() {
    return channels;
  }
}
