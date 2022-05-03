package io.citadel.kernel.domain.service;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.repository.Repository;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public enum Defaults {
  Companion;

  public Domain.Verticle verticle() {
    return new Service();
  }

  static <A extends Domain.Aggregate, I extends Domain.ID<?>, M extends Record> Domain.Aggregates<A, I, M> aggregates(EventStore eventStore, Domain.Snapshot<A, M> snapshot, String name) {
    return new Repository<>(eventStore, snapshot, name);
  }

  public static final class Service extends AbstractVerticle implements Domain.Verticle {
    @Override
    public void start(final Promise<Void> start) throws Exception {
      super.start(start);
    }
  }
}
