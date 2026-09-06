package org.example.backend_fundamentals.low_level_design.case_studies.notification_lld.playground.model;

/**
 * Contact endpoints for one user across supported channels.
 */
public class Recipient {
  private final String userId;
  private final String email;
  private final String phoneNumber;
  private final String deviceToken;

  public Recipient(String userId, String email, String phoneNumber, String deviceToken) {
    if (isBlank(userId)) {
      throw new IllegalArgumentException("userId is required");
    }
    this.userId = userId;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.deviceToken = deviceToken;
  }

  public String getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getDeviceToken() {
    return deviceToken;
  }

  static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
