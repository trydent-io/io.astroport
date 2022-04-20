package io.citadel.kernel.domain;

import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.repository.Repository;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.media.Json;
import io.vertx.core.Future;

import java.util.stream.Stream;

public sealed interface Domain {
  enum Namespace implements Domain {}

  interface State<S extends Enum<S>> {}

  interface Command {}
  interface Event {
    default EventInfo asInfo() {
      return new EventInfo(getClass().getSimpleName(), Json.with(this));
    }
  }

  interface Aggregates<A extends Aggregate, I extends Domain.ID> {
    static <A extends Aggregate, I extends Domain.ID> Aggregates<A, I> repository(EventStore eventStore, Domain.Snapshot<A> snapshot, String name) {
      return new Repository<>(eventStore, snapshot, name);
    }

    Future<A> lookup(I id);
    default Future<Void> persist(AggregateInfo aggregate, Stream<EventInfo> events) {
      return persist(aggregate, events, null);
    }
    Future<Void> persist(AggregateInfo aggregate, Stream<EventInfo> events, String by);
  }

  interface Snapshot<A extends Aggregate> {
    A aggregate(String id, long version, Stream<EventInfo> events) throws Throwable;
  }
  interface Aggregate {
    <T> T commit(ThrowableBiFunction<? super AggregateInfo, ? super Stream<EventInfo>, ? extends T> transaction);
  }
  interface Lifecycle<M> {
    <R> R eventually(ThrowableFunction<? super M, ? extends R> then);
  }

  interface ID extends Attribute<String> {}
}

