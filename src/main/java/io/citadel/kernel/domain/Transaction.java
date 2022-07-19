package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.eventstore.metadata.Change;
import io.citadel.kernel.eventstore.event.ID;
import io.citadel.kernel.eventstore.event.Name;
import io.citadel.kernel.eventstore.event.Version;
import io.citadel.kernel.lang.stream.Streamed;
import io.vertx.core.Future;

import java.util.stream.Stream;

public sealed interface Transaction<E> extends Committable {
  record Aggregate(ID id, Name name, Version version) {}
  static <S extends Enum<S> & State<S, E>, E> Transaction<E> open(EventStore pool, Aggregate aggregate, S state) {
    return new Open<>(pool, aggregate, state, Stream.empty());
  }

  <T extends Enum<T> & State<T, F>, F> Transaction<F> fork(Aggregate aggregate, T state);
  Transaction<E> log(E event);
  final class Open<S extends Enum<S> & State<S, E>, E> implements Transaction<E>, Streamed<Change> {

    private final EventStore pool;
    private final Aggregate aggregate;
    private final S state;
    private final Stream<Change> changes;

    private Open(EventStore pool, Aggregate aggregate, S state, Stream<Change> changes) {
      this.pool = pool;
      this.aggregate = aggregate;
      this.state = state;
      this.changes = changes;
    }

    @Override
    public <T extends Enum<T> & State<T, F>, F> Transaction<F> fork(Aggregate aggregate, T state) {
      return new Open<>(pool, aggregate, state, changes);
    }

    @Override
    public Transaction<E> log(E event) {
      return state.transitable(event)
        ? new Open<>(pool, aggregate, state.transit(event), append(changes))
        : this;
    }

    @Override
    public Future<Void> commit() {
      return pool.store(aggregate.id, aggregate.name, aggregate.version, io.citadel.kernel.eventstore.metadata.State.from(state), changes).mapEmpty();
    }
  }
}
