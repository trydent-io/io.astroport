package io.citadel.kernel.domain.service;

import io.citadel.kernel.domain.Domain;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public enum Defaults {
  Companion;

  public Domain.Verticle verticle() {
    return new Service();
  }

  public static final class Service extends AbstractVerticle implements Domain.Verticle {
    @Override
    public void start(final Promise<Void> start) throws Exception {
      super.start(start);
    }
  }
}
