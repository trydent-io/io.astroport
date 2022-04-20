package io.citadel.kernel.domain;

import io.vertx.core.eventbus.EventBus;

final class Messages<M extends Domain.Message> implements Domain.Message.Bus {
  private final EventBus bus;

  Messages(EventBus bus) {
    this.bus = bus;
  }
}
