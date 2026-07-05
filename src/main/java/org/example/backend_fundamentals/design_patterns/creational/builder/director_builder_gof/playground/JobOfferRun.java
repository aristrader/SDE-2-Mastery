package org.example.backend_fundamentals.design_patterns.creational.builder.director_builder_gof.playground;

import lombok.extern.slf4j.Slf4j;

/**
 * Demo runner for the GoF Director-Builder. Headline scenario is "same
 * senior recipe, two builders, two artefacts" &mdash; one structured
 * {@link JobOffer}, one multi-line letter. Third scenario shows a different
 * recipe on the same builder for contrast.
 */
@Slf4j
public class JobOfferRun {

  public static void main(String[] args) {
    OfferDirector director = new OfferDirector();

    log.info("=== 1. Senior recipe -> JobOfferBuilder produces a JobOffer ===");
    JobOfferBuilder jobOfferBuilder = new JobOfferBuilder(200_000, "BANGALORE");
    director.constructSeniorOffer(jobOfferBuilder);
    JobOffer offer = jobOfferBuilder.build();
    System.out.println(offer);

    log.info("=== 2. SAME senior recipe -> OfferLetterBuilder produces a letter ===");
    OfferLetterBuilder letterBuilder = new OfferLetterBuilder("Asha", 200_000, "BANGALORE");
    director.constructSeniorOffer(letterBuilder);
    String seniorLetter = letterBuilder.build();
    System.out.println(seniorLetter);

    log.info("=== 3. Junior recipe -> OfferLetterBuilder produces a different letter ===");
    OfferLetterBuilder juniorLetterBuilder = new OfferLetterBuilder("Rohan", 80_000, "PUNE");
    director.constructJuniorOffer(juniorLetterBuilder);
    String juniorLetter = juniorLetterBuilder.build();
    System.out.println(juniorLetter);
  }
}
