package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method_basic;

/**
 * Abstract creator in the GoF Factory Method pattern &mdash; learning variant.
 *
 * <p>Owns the <b>template method</b> {@link #onboard()} that runs a 5-step developer hiring
 * flow. Concrete subclasses plug in the product by overriding {@link #createDeveloper()}.
 *
 * <p><b>Learning-focused simplifications vs. the production variant:</b>
 *
 * <ul>
 *   <li>No {@code HiringProcess} interface above this class &mdash; the only hiring flow in
 *       this package is for developers.
 *   <li>Services ({@link EmailService}, {@link OfferLetterService}) are called statically, so
 *       this class holds no collaborator fields and needs no constructor.
 *   <li>Concrete subclasses are therefore constructor-less too.
 * </ul>
 *
 * <p>The trade-offs these simplifications impose are discussed in
 * {@code FactoryMethodBasic.md} and contrasted with the production variant in
 * {@code Factory.md}.
 */
public abstract class DeveloperHiringProcess {

  /**
   * Template method &mdash; fixed sequence of hiring steps. Marked {@code final} so subclasses
   * cannot replace the algorithm; they only plug in {@link #createDeveloper()}.
   */
  public final Employee onboard() {
    System.out.println(
        "=== Starting hiring process for "
            + this.getClass().getSimpleName().replace("HiringProcess", "")
            + " role ===");

    checkBudget();
    Employee developer = createDeveloper();
    provisionLaptop(developer);
    String email = EmailService.createEmailAccount(developer);
    OfferLetterService.sendOfferLetter(developer, email);

    System.out.println("  [Done]   Onboarded " + developer.getTitle() + " successfully\n");
    return developer;
  }

  /** The factory method. Each concrete subclass returns the specific {@link Employee}. */
  protected abstract Employee createDeveloper();

  private void checkBudget() {
    System.out.println("  [Budget] Quarterly hiring budget check: PASSED");
  }

  private void provisionLaptop(Employee developer) {
    System.out.println("  [Laptop] Provisioned laptop for " + developer.getTitle());
  }
}
