package com.example.project;

import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class StepVerifierDemo {

  @Test
  void expectNextMatchesMatchesNextFluxElement() {
    StepVerifier
      .create(Flux.fromIterable(List.of(1, 2, 3)))
      .expectNextMatches(integer -> integer == 1)
      .thenCancel()
      .verify();
  }

  @Test
  void expectNextCanBeUsedToMatchAnyElement() {
    var publisher = Flux.fromIterable(List.of(1, 2, 3));
    StepVerifier
      .create(publisher.filter(integer -> integer == 3))
      .expectNextMatches(integer -> integer == 3)
      .thenCancel()
      .verify();
  }
}
