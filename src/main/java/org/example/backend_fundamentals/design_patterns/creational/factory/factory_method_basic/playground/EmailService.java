package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method_basic.playground;

/**
 * Static utility used by {@link DeveloperHiringProcess#onboard()} to provision a new email
 * account for a hired developer.
 *
 * <p>Intentionally <b>static</b> in this learning variant &mdash; keeps the pattern focus on
 * Factory Method rather than on dependency injection or collaborator lifecycle. See the
 * production variant ({@code factory_method/EmailService}) for the instance-based version.
 */
public class EmailService {

  private EmailService() {}

  public static String createEmailAccount(Employee developer) {
    String email = developer.getTitle().toLowerCase().replace(" ", ".") + "@company.com";
    System.out.println("  [Email]  Created email account: " + email);
    return email;
  }
}
