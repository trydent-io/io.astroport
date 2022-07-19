package io.citadel.kernel.domain;

import io.citadel.kernel.domain.model.Defaults;
import io.citadel.kernel.domain.model.Service;
import io.citadel.kernel.eventstore.event.Name;
import io.vertx.core.Future;

public interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {
  }

  static <T, R extends Record, F, N extends Enum<N> & State<N, F>> Model<T, R, F, N> model(String name) {
    return new Model.Local<>(Name.of(name));
  }

  interface Handler<A, R extends Record> {
    Future<Void> handle(A aggregate, R record);
  }
}

