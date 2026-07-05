package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method;

/**
 * Collaborator used by {@link DeveloperHiringProcess#onboard()} to send an offer letter to a
 * newly provisioned developer.
 *
 * <p>Instance-based by design (methods are <b>not</b> {@code static}): the class is meant to
 * be held as a field on the hiring-process classes so it can be mocked in tests, swapped for a
 * different implementation, or configured with state in the future. In a Spring application
 * this would be a {@code @Service} bean.
 */
public class OfferLetterService {

  /**
   * Prints a simulated offer-letter delivery to the console.
   *
   * @param developer the newly hired employee
   * @param email the email address provisioned by {@link EmailService}
   */
  public void sendOfferLetter(Employee developer, String email) {
    System.out.println(
        "  [Offer]  Sent offer letter to " + email
            + " for " + developer.getTitle()
            + " role (salary: " + developer.getSalary() + ")");
  }
}
