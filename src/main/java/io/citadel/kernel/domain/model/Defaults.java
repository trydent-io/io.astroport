package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;

import java.util.stream.Stream;

public enum Defaults {
  Companion;

  public Domain.Verticle verticle() {
    return new Service();
  }

  public <A extends Domain.Aggregate, I extends Domain.ID<?>, M extends Record & Domain.Model<I>> Domain.Lookup<I, M, A> lookup(EventStore eventStore, Domain.Snapshot<M, A, S> snapshot, String name) {
    return new Aggregates<>(eventStore, snapshot, name);
  }

  public Domain.Transaction transaction(EventStore eventStore) {
    return new Changes(eventStore, Stream.empty());
  }
}
