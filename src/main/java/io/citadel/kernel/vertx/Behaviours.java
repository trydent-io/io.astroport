package io.citadel.kernel.vertx;

import io.vertx.core.eventbus.EventBus;

sealed public interface Behaviours {
  static Behaviours registry(EventBus eventBus) {
    return new Registry(eventBus);
  }

  <R extends java.lang.Record, H extends Task.Handler<R>> Behaviours be(Class<R> type, String address, H handler);

  EventBus configure();
}

record Registry(EventBus eventBus) implements Behaviours {
  @Override
  public <R extends java.lang.Record, H extends Task.Handler<R>> Behaviours be(final Class<R> type, final String address, final H handler) {
    eventBus
      .registerDefaultCodec(type, Codec.forRecord(type))
      .localConsumer(address, handler);
    return this;
  }

  @Override
  public EventBus configure() {
    return eventBus;
  }
}
