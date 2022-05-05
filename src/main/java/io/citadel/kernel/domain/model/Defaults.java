package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;

import java.util.stream.Stream;

public enum Defaults {
  Companion;

  public Domain.Verticle verticle() {
    return new Service();
  }

  public <A extends Domain.Aggregate, I extends Domain.ID<?>, M extends Record> Domain.Aggregates<A, I, M> aggregates(EventStore eventStore, Domain.Snapshot<A, M> snapshot, String name) {
    return new Repository<>(eventStore, snapshot, name);
  }

  static Domain.Transaction transaction(EventStore eventStore) {
    return new Changes(eventStore, Stream.empty());
  }
}
