package io.citadel.domain;

import io.vertx.core.AbstractVerticle;

public sealed interface Domain {
  static Domain.Verticle verticle() {
    return new Service();
  }

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {}

  final class Service extends AbstractVerticle implements Domain.Verticle {

  }
}
