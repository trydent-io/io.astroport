package io.citadel.kernel.domain;

import io.citadel.kernel.domain.context.Context;
import io.citadel.kernel.domain.model.Defaults;
import io.citadel.kernel.domain.model.Service;
import io.citadel.kernel.eventstore.metadata.Aggregate.Root;
import io.citadel.kernel.eventstore.metadata.Name;
import io.vertx.core.eventbus.Message;

public interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {
  }

  static <T, R extends Record, F, N extends Enum<N> & State<N, F>> Model<T, R, F, N> model(String name) {
    return new Model.Impl<>(Name.of(name));
  }

  interface Handler<ID, M extends Record, E, S extends Enum<S> & State<S, E>, R extends Record> {
    Context<Root<ID, M, E, S>> handle(Headers headers, Message<R> message, Context<Root<ID, M, E, S>> context, R command);
  }
}

