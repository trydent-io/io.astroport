package io.citadel;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;

public sealed interface Citadel {
  static Citadel domain() {
    return new Domain();
  }
  static Citadel service() { return new Domain(); }

  default Verticle asVerticle() {
    return switch (this) { case Domain domain -> domain; };
  }

  final class Domain extends AbstractVerticle implements Citadel {

  }
}
