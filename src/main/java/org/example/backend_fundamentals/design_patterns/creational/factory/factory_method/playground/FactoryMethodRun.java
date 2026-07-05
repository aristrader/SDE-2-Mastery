package org.example.backend_fundamentals.design_patterns.creational.factory.factory_method.playground;

/**
 * Demo runner for the production Factory Method variant.
 *
 * <p>Wires shared services, creates one concrete creator per role, and runs three onboarding
 * flows. {@link HR} depends only on {@link HiringProcess} — no concrete developer class named.
 */
public class FactoryMethodRun {

  public static void main(String[] args) {
    // Shared collaborators — one instance each, reused across every hiring process.
    EmailService emailService = new EmailService();
    OfferLetterService offerLetterService = new OfferLetterService();

    // One concrete creator per role. Each knows which Employee subclass to build.
    AndroidHiringProcess androidHiringProcess =
        new AndroidHiringProcess(emailService, offerLetterService);
    BackendHiringProcess backendHiringProcess =
        new BackendHiringProcess(emailService, offerLetterService);
    IosHiringProcess iosHiringProcess =
        new IosHiringProcess(emailService, offerLetterService);

    // One HR per hiring flow. HR depends only on the HiringProcess interface.
    HR hrForAndroid = new HR(androidHiringProcess);
    HR hrForBackend = new HR(backendHiringProcess);
    HR hrForIos = new HR(iosHiringProcess);

    hrForAndroid.hireForTeam();
    hrForBackend.hireForTeam();
    hrForIos.hireForTeam();
  }
}
