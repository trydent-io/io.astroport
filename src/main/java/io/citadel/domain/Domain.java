package io.citadel.domain;

import io.vertx.core.AbstractVerticle;

public sealed interface Domain {
  static Domain service() {
    return new Service();
  }

  default Verticle asVerticle() {
    return switch (this) { case Service service -> service; };
  }

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {}

  final class Service extends AbstractVerticle implements Domain.Verticle {

  }
}
