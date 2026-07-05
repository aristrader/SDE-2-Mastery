package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method;

/**
 * Collaborator used by {@link DeveloperHiringProcess#onboard()} to provision a new email
 * account for a hired developer.
 *
 * <p>Instance-based by design (methods are <b>not</b> {@code static}): the class is meant to
 * be held as a field on the hiring-process classes so it can be mocked in tests, swapped for a
 * different implementation, or configured with state (e.g., company domain, SMTP client) in
 * the future. In a Spring application this would be a {@code @Service} bean.
 */
public class EmailService {

  /**
   * Builds a predictable email address from the developer's role title, prints a log line,
   * and returns the address.
   *
   * @param developer the newly hired employee
   * @return the provisioned email address, e.g. {@code "android.developer@company.com"}
   */
  public String createEmailAccount(Employee developer) {
    String email = developer.getTitle().toLowerCase().replace(" ", ".") + "@company.com";
    System.out.println("  [Email]  Created email account: " + email);
    return email;
  }
}
