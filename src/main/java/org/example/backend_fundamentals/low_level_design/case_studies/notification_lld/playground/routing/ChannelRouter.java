package org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.routing;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.sender.NotificationSender;

/**
 * Registry that maps a delivery channel to its sender strategy.
 */
public class ChannelRouter {
  private final Map<Channel, NotificationSender> senders = new EnumMap<>(Channel.class);

  public ChannelRouter(List<NotificationSender> senders) {
    if (senders == null || senders.isEmpty()) {
      throw new IllegalArgumentException("senders are required");
    }
    for (NotificationSender sender : senders) {
      this.senders.put(sender.channel(), sender);
    }
  }

  public NotificationSender senderFor(Channel channel) {
    NotificationSender sender = senders.get(channel);
    if (sender == null) {
      throw new IllegalArgumentException("No sender registered for channel: " + channel);
    }
    return sender;
  }
}
