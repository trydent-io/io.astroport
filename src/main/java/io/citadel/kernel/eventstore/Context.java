package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;

import java.util.stream.Stream;

public final class Context<M extends Record & Domain.Model<?>, E extends Domain.Event, S extends Enum<S> & Domain.State<S, E>> {
  private final M model;
  private final String name;
  private final long version;
  private final S state;
  private final Stream<E> changes;

  public Context(M model, String name, long version, S state) {
    this(model, name, version, state, Stream.empty());
  }
  public Context(M model, String name, long version, S state, Stream<E> changes) {
    this.model = model;
    this.name = name;
    this.version = version;
    this.state = state;
    this.changes = changes;
  }
}
