package io.citadel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vertx.core.Vertx.vertx;

public sealed interface Main {
  enum Namespace implements Main {;
    private static final Logger log = LoggerFactory.getLogger(Main.class);
  }

  static void main(String[] args) {
    final var vertx = vertx();
    vertx
      .deployVerticle(Citadel.service(vertx).asVerticle())
      .onSuccess(it -> Namespace.log.info("Main service has been deployed with id %s".formatted(it)))
      .onFailure(it -> Namespace.log.error("Can't deploy main service", it))
      .onFailure(it -> vertx.close());
  }
}
