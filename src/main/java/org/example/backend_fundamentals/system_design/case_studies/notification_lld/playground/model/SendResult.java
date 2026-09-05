package org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.model;

/**
 * Per-channel send outcome.
 */
public class SendResult {
  private final Channel channel;
  private final boolean success;
  private final String providerMessageId;
  private final String errorMessage;

  private SendResult(Channel channel, boolean success, String providerMessageId,
      String errorMessage) {
    this.channel = channel;
    this.success = success;
    this.providerMessageId = providerMessageId;
    this.errorMessage = errorMessage;
  }

  public static SendResult success(Channel channel, String providerMessageId) {
    return new SendResult(channel, true, providerMessageId, null);
  }

  public static SendResult failure(Channel channel, String errorMessage) {
    return new SendResult(channel, false, null, errorMessage);
  }

  public Channel getChannel() {
    return channel;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getProviderMessageId() {
    return providerMessageId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public String toString() {
    return "SendResult{"
        + "channel=" + channel
        + ", success=" + success
        + ", providerMessageId='" + providerMessageId + '\''
        + ", errorMessage='" + errorMessage + '\''
        + '}';
  }
}
