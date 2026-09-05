package org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.rendering;

import java.util.Map;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.Channel;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.NotificationMessage;
import org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model.NotificationRequest;

public class SimpleTemplateRenderer implements TemplateRenderer {
  @Override
  public NotificationMessage render(NotificationRequest request, Channel channel) {
    Map<String, String> data = request.getTemplateData();

    switch (request.getType()) {
      case OTP:
        return new NotificationMessage(
            request.getType(),
            channel,
            request.getRecipient(),
            "Your OTP",
            "Your OTP",
            "Your OTP is " + data.get("otp")
        );
      case PAYMENT_SUCCESS:
        return new NotificationMessage(
            request.getType(),
            channel,
            request.getRecipient(),
            "Payment successful",
            "Payment successful",
            "Your payment of " + data.get("amount") + " succeeded"
        );
      case KYC_APPROVED:
        return new NotificationMessage(
            request.getType(),
            channel,
            request.getRecipient(),
            "KYC approved",
            "KYC approved",
            "Your KYC verification is approved"
        );
      case MARKETING:
        return new NotificationMessage(
            request.getType(),
            channel,
            request.getRecipient(),
            "Offer for you",
            "Offer for you",
            data.get("message")
        );
      default:
        throw new IllegalArgumentException("Unsupported notification type: " + request.getType());
    }
  }
}
