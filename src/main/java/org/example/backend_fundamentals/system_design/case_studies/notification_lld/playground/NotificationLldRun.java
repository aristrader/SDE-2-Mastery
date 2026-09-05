package org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.client.ConsoleEmailProviderClient;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.client.ConsolePushProviderClient;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.client.ConsoleSmsProviderClient;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.NotificationRequest;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.NotificationType;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.Recipient;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.SendResult;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.rendering.SimpleTemplateRenderer;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.routing.ChannelRouter;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.sender.EmailNotificationSender;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.sender.PushNotificationSender;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.sender.SmsNotificationSender;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.service.NotificationService;

public class NotificationLldRun {

  public static void main(String[] args) {
    NotificationService notificationService = new NotificationService(
        new SimpleTemplateRenderer(),
        new ChannelRouter(Arrays.asList(
            new EmailNotificationSender(new ConsoleEmailProviderClient()),
            new SmsNotificationSender(new ConsoleSmsProviderClient()),
            new PushNotificationSender(new ConsolePushProviderClient())
        ))
    );

    Map<String, String> templateData = new HashMap<>();
    templateData.put("otp", "731902");

    NotificationRequest request = new NotificationRequest(
        NotificationType.OTP,
        new Recipient("user-1", "user@example.com", "+919999999999", "device-token-1"),
        templateData,
        Arrays.asList(Channel.EMAIL, Channel.SMS, Channel.PUSH)
    );

    List<SendResult> results = notificationService.send(request);
    for (SendResult result : results) {
      System.out.println(result);
    }
  }
}
