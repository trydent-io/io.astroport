package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.EventStore;

public interface Logs {

}

interface Aggregation<M extends Record & Domain.Model<?>, E extends Domain.Event> {}

final class Local implements Logs {
  private final Domain.ID<?> id;
  private final String name;
  private final Domain.Lookup<M, E> lookup;

  public <M extends Record & Domain.Model<?>, E extends Domain.Event> Object compose(Aggregation<M, E> aggregation) {
    lookup.findLogs(id, name)
      .
  }
}
