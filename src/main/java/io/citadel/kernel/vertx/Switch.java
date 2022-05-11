package io.citadel.kernel.vertx;

import io.citadel.kernel.domain.Domain;
import io.vertx.core.eventbus.EventBus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Switch {
  static Switch handlers() {
    return new Handlers(new ConcurrentHashMap<>());
  }

  <R extends Codec> Switch bind(String address, Domain.Handler<R> handler);
  EventBus apply(EventBus eventBus);
}

final class Handlers implements Switch {
  private final Map<String, Domain.Handler<?>> handlers;

  Handlers(Map<String, Domain.Handler<?>> handlers) {
    this.handlers = handlers;
  }

  @Override
  public <R extends Codec> Switch bind(String address, Domain.Handler<R> handler) {
    handlers.put(address, handler);
    return this;
  }

  @Override
  public EventBus apply(EventBus eventBus) {
    handlers.forEach(eventBus::localConsumer);
    return eventBus;
  }
}
