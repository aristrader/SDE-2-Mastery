package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method.playground;

/** Concrete creator — plugs an {@link AndroidDeveloper} into the hiring flow. */
public class AndroidHiringProcess extends DeveloperHiringProcess {

  protected AndroidHiringProcess(EmailService emailService, OfferLetterService offerLetterService) {
    super(emailService, offerLetterService);
  }

  @Override
  public Employee createDeveloper() {
    return new AndroidDeveloper();
  }
}
