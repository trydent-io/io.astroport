package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public final class Service extends AbstractVerticle implements Domain.Verticle {
  @Override
  public void start(final Promise<Void> start) throws Exception {
    super.start(start);
  }
}
