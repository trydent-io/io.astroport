package io.citadel.context;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;

public sealed interface Context {
  static Context service() {
    return new Service();
  }

  default Verticle asVerticle() {
    return switch (this) { case Service service -> service; };
  }

  final class Service extends AbstractVerticle implements Context {

  }
}
