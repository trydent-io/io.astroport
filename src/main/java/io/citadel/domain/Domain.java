package io.citadel.domain;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public sealed interface Domain {
  static Domain.Verticle verticle() {
    return new Service();
  }

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {}

  final class Service extends AbstractVerticle implements Domain.Verticle {
    @Override
    public void start(final Promise<Void> start) throws Exception {
      super.start(start);
    }
  }
}
