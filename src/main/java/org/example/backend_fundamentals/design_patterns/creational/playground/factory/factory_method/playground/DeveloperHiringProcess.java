package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method;

/**
 * Abstract creator for the developer hiring flow.
 *
 * <p>Owns the template method {@link #onboard()} (budget → create → laptop → email → offer).
 * Concrete subclasses override only {@link #createDeveloper()}; the algorithm is {@code final}.
 * Collaborators are injected via constructor — the reason this is an abstract class, not an interface.
 */
public abstract class DeveloperHiringProcess implements HiringProcess {

  private final EmailService emailService;
  private final OfferLetterService offerLetterService;

  /**
   * @param emailService creates email accounts for new hires
   * @param offerLetterService delivers offer letters to new hires
   */
  protected DeveloperHiringProcess(
      EmailService emailService, OfferLetterService offerLetterService) {
    this.emailService = emailService;
    this.offerLetterService = offerLetterService;
  }

  /** Template method — {@code final} so subclasses plug in only {@link #createDeveloper()}. */
  @Override
  public final Employee onboard() {
    System.out.println(
        "=== Starting hiring process for "
            + this.getClass().getSimpleName().replace("HiringProcess", "")
            + " role ===");

    checkBudget();
    Employee developer = createDeveloper();
    provisionLaptop(developer);
    String email = emailService.createEmailAccount(developer);
    offerLetterService.sendOfferLetter(developer, email);

    System.out.println("  [Done]   Onboarded " + developer.getTitle() + " successfully\n");
    return developer;
  }

  /** The factory method. Each subclass returns its specific {@link Employee}. */
  protected abstract Employee createDeveloper();

  private void checkBudget() {
    System.out.println("  [Budget] Quarterly hiring budget check: PASSED");
  }

  private void provisionLaptop(Employee developer) {
    System.out.println("  [Laptop] Provisioned laptop for " + developer.getTitle());
  }
}
