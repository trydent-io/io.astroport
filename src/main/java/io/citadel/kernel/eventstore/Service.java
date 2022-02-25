package io.citadel.kernel.eventstore;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

final class Service extends AbstractVerticle implements EventStore {
  private final EventStore eventStore;

  @Override
  public void start(final Promise<Void> start) {
    vertx.eventBus().<JsonObject>localConsumer(operations.RESTORE, message ->
      findBy(message.body().mapTo(UUID.class))
        .onSuccess(message::reply)
        .onFailure(it -> message.fail(500, it.getMessage()))
    );

    vertx.eventBus().<JsonObject>localConsumer(operations.PERSIST, message ->
      persist(message.body().mapTo())
    );

    start.complete();
  }
}
