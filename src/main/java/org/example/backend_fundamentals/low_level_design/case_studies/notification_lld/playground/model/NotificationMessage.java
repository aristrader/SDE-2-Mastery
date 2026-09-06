package org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model;

/**
 * Channel-specific rendered message ready for a sender.
 */
public class NotificationMessage {
  private final NotificationType type;
  private final Channel channel;
  private final Recipient recipient;
  private final String title;
  private final String subject;
  private final String body;

  public NotificationMessage(NotificationType type, Channel channel, Recipient recipient,
      String title, String subject, String body) {
    this.type = type;
    this.channel = channel;
    this.recipient = recipient;
    this.title = title;
    this.subject = subject;
    this.body = body;
  }

  public NotificationType getType() {
    return type;
  }

  public Channel getChannel() {
    return channel;
  }

  public Recipient getRecipient() {
    return recipient;
  }

  public String getTitle() {
    return title;
  }

  public String getSubject() {
    return subject;
  }

  public String getBody() {
    return body;
  }
}
