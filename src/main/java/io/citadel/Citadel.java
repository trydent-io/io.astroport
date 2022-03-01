package io.citadel;

import io.vertx.core.AbstractVerticle;

public sealed interface Citadel {
  static Citadel domain() {
    return new Domain();
  }
  static Citadel service() { return new Main(new Domain(), eventStore); }

  final class Domain extends AbstractVerticle implements Citadel {

  }
}
