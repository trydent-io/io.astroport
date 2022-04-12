package io.citadel.domain.forum.handler;

import io.citadel.kernel.domain.Domain;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;

public enum Handlers {
  Companion;

  public void bind(Domain.Bus bus)

  final class Register implements Domain.Handler<Commands.Register> {
    @Override
    public void handle(final Message<Commands.Register> message, final Commands.Register content, final MultiMap headers) {
    }
  }
}
