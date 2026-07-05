package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method.playground;

/** Concrete creator — plugs a {@link BackendDeveloper} into the hiring flow. */
public class BackendHiringProcess extends DeveloperHiringProcess {

  protected BackendHiringProcess(EmailService emailService, OfferLetterService offerLetterService) {
    super(emailService, offerLetterService);
  }

  @Override
  public Employee createDeveloper() {
    return new BackendDeveloper();
  }
}
