package io.citadel;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;

public sealed interface Citadel extends Verticle permits Main {
  static Citadel domain() {
    return new Domain();
  }
  static Citadel service() { return new Main(new Domain()); }

  final class Domain extends AbstractVerticle implements Citadel {

  }
}
