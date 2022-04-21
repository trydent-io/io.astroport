package io.citadel.kernel.domain;

import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.repository.Repository;
import io.citadel.kernel.domain.service.Defaults;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.Future;

import java.util.stream.Stream;

public sealed interface Domain {
  Defaults defaults = Defaults.Companion;
  sealed interface Verticle extends io.vertx.core.Verticle permits Defaults.Service {}

  interface State<S extends Enum<S>> {}

  interface Command {}
  interface Event {}

  interface Aggregates<A extends Aggregate, I extends Domain.ID<?>> {
    static <A extends Aggregate, I extends Domain.ID<?>> Aggregates<A, I> repository(EventStore eventStore, Domain.Snapshot<A> snapshot, String name) {
      return new Repository<>(eventStore, snapshot, name);
    }

    Future<A> lookup(I id);
    default Future<Void> persist(AggregateInfo aggregate, Stream<EventInfo> events) {
      return persist(aggregate, events, null);
    }
    Future<Void> persist(AggregateInfo aggregate, Stream<EventInfo> events, String by);
  }

  interface Snapshot<A extends Aggregate> {
    A aggregate();
  }
  interface Aggregate {
    <T> T commit(ThrowableBiFunction<? super AggregateInfo, ? super Stream<EventInfo>, ? extends T> transaction);
  }
  interface Seed<M> {
    <R> R eventually(ThrowableFunction<? super M, ? extends R> then);
  }

  interface ID<T> extends Attribute<T> {}
}

