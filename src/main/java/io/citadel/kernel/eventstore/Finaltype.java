package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;

import java.util.stream.Stream;

public final class Finaltype<M extends Record & Domain.Model<?>, E extends Domain.Event, S extends Enum<S> & Domain.State<S, E>> {
  private final Aggregate<M> aggregate;
  private final S state;
  private final Stream<E> changes;

  Finaltype(final Aggregate<M> aggregate, final S state, final Stream<E> changes) {
    this.aggregate = aggregate;
    this.state = state;
    this.changes = changes;
  }
}
