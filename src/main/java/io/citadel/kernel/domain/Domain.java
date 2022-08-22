package io.citadel.kernel.domain;

import io.citadel.kernel.domain.model.Defaults;
import io.citadel.kernel.domain.model.Service;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.Future;

public interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {
  }

  static Domain with(EventStore eventStore) {

  }

  interface Handler<AGGREGATE, R extends Record> {
    Future<Void> handle(AGGREGATE aggregate, R record);
  }
}

