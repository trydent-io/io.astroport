package io.citadel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.citadel.Main.Namespace.*;
import static io.vertx.core.Vertx.vertx;

public sealed interface Main {
  enum Namespace implements Main {;
    static final Logger log = LoggerFactory.getLogger(Main.class);
  }

  static void main(String[] args) {
    final var vertx = vertx();
    vertx.eventBus().consumer("").handler()
    vertx
      .deployVerticle(Citadel.verticle(vertx))
      .onSuccess(it -> log.info("Main service has been deployed with id %s".formatted(it)))
      .onFailure(it -> log.error("Can't deploy main service", it))
      .onFailure(it -> vertx.close());
  }
}
