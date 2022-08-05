package io.citadel.domain.forum;

import io.citadel.kernel.domain.Aggregate;
import io.citadel.kernel.domain.Aggregates;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.TryFunction;

public interface Hydration extends TryFunction<Forum.ID, Forum> {
  static Hydration lookup(EventStore eventStore) {
    return new Lookup(Aggregates.repository(eventStore));
  }
}

final class Lookup implements Hydration, Aggregates<Aggregate<Forum.Entity, Forum.State>> {
  private final Aggregates<Aggregate<Forum.Entity, Forum.State>> aggregates;

  Lookup(Aggregates<Aggregate<Forum.Entity, Forum.State>> aggregates) {
    this.aggregates = aggregates;
  }
}
