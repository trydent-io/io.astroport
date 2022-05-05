package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;

import java.util.stream.Stream;

public enum Defaults {
  Companion;

  public Domain.Verticle verticle() {
    return new Service();
  }

  public <A extends Domain.Aggregate, I extends Domain.ID<?>, M extends Record> Domain.Models<A, I, M> repository(EventStore eventStore, Domain.Snapshot<A, M> snapshot, String name) {
    return new Snapshots<>(eventStore, snapshot, name);
  }

  static Domain.Transaction transaction(EventStore eventStore) {
    return new Changes(eventStore, Stream.empty());
  }
}
