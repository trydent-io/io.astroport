package io.citadel;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;

public sealed interface Citadel extends Verticle {
  static Citadel service() {
    return new Service();
  }
}

final class Service extends AbstractVerticle implements Citadel {
  private final Forums forums;
}
