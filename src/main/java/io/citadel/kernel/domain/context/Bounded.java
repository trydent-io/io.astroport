package io.citadel.kernel.domain.context;

import io.citadel.kernel.domain.State;
import io.citadel.kernel.eventstore.EventPool;
import io.citadel.kernel.eventstore.metadata.Change;

import java.util.stream.Stream;

public sealed interface Bounded<E> {

  static <S extends Enum<S> & State<S, E>, E> Bounded<E> context(EventPool pool, S state) {
    return new Context<>(pool, state, Stream.empty());
  }
}

final class Context<S extends Enum<S> & State<S, E>, E> implements Bounded<E> {
  private final EventPool pool;
  private final S state;
  private final Stream<Change> changes;

  Context(EventPool pool, S state, Stream<Change> changes) {
    this.pool = pool;
    this.state = state;
    this.changes = changes;
  }
}
