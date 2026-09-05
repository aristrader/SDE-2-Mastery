package org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.service;

import java.util.ArrayList;
import java.util.List;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.NotificationMessage;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.NotificationRequest;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.SendResult;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.rendering.TemplateRenderer;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.routing.ChannelRouter;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.sender.NotificationSender;

/**
 * Facade for notification orchestration.
 */
public class NotificationService {
  private final TemplateRenderer templateRenderer;
  private final ChannelRouter channelRouter;

  public NotificationService(TemplateRenderer templateRenderer, ChannelRouter channelRouter) {
    this.templateRenderer = templateRenderer;
    this.channelRouter = channelRouter;
  }

  public List<SendResult> send(NotificationRequest request) {
    List<SendResult> results = new ArrayList<>();

    for (Channel channel : request.getChannels()) {
      NotificationMessage message = templateRenderer.render(request, channel);
      NotificationSender sender = channelRouter.senderFor(channel);
      results.add(sender.send(message));
    }

    return results;
  }
}
