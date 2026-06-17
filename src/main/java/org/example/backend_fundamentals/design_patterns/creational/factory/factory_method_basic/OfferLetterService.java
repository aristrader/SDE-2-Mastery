package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method_basic;

/**
 * Static utility used by {@link DeveloperHiringProcess#onboard()} to send an offer letter to a
 * newly provisioned developer.
 *
 * <p>Intentionally <b>static</b> in this learning variant &mdash; see
 * {@code factory_method/OfferLetterService} for the instance-based production version.
 */
public class OfferLetterService {

  private OfferLetterService() {}

  public static void sendOfferLetter(Employee developer, String email) {
    System.out.println(
        "  [Offer]  Sent offer letter to " + email
            + " for " + developer.getTitle()
            + " role (salary: " + developer.getSalary() + ")");
  }
}
