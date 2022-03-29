package io.citadel.kernel.domain;

import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.data.MetaAggregate;
import io.citadel.eventstore.data.MetaEvent;
import io.citadel.kernel.domain.repository.Repository;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableSupplier;
import io.citadel.kernel.media.Json;
import io.vertx.core.Future;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public sealed interface Domain {
  enum Namespace implements Domain {}

  @SuppressWarnings("unchecked")
  interface State<S extends Enum<S>> {
    default Stream<S> or(S... states) {return Stream.concat(Stream.of((S) this), Stream.of(states));}

    default boolean is(S... states) {
      return List.of(states).contains((S) this);
    }
  }

  interface Command {}

  interface Event {
    default MetaEvent asMeta() {
      return new MetaEvent(getClass().getSimpleName(), Json.with(this));
    }
  }

  interface Entity<A extends Aggregate<A>, I extends Domain.ID<?>> {
    A aggregate(I id, long version);
  }

  interface Aggregate<A extends Aggregate<A>> {}

  interface Aggregates<A extends Aggregate<A>, I extends ID<?>, E extends Domain.Event> {
    static <A extends Aggregate<A>, I extends ID<?>, E extends Domain.Event> Aggregates<A, I, E> repository(EventStore eventStore, Domain.Hydration<A> hydration, String name) {
      return new Repository<>(eventStore, hydration, name);
    }

    Future<A> load(I id);
    Future<Void> save(I id, long version, Stream<E> events);
  }

  interface Snapshot<T extends Transaction> {
    T transaction(long version);
  }

  interface Hydration<S extends Snapshot<?>> {
    S snapshot(long version, Stream<MetaEvent> events) throws Throwable;
  }
  interface Transaction {
    Future<Void> commit(ThrowableBiFunction<? super MetaAggregate, ? super Stream<MetaEvent>, ? extends Future<Void>> transaction);
  }

  interface ValueObject<R extends Record & ValueObject<R>> {}

  interface ID<R extends Record & ID<R>> extends ValueObject<R> {}
}

