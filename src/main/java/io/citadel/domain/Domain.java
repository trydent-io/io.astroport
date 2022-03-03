package io.citadel.domain;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;

public sealed interface Domain {
  static Domain service() {
    return new Service();
  }

  default Verticle asVerticle() {
    return switch (this) { case Service service -> service; };
  }

  final class Service extends AbstractVerticle implements Domain {

  }
}
