package io.citadel.kernel.domain;

import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.repository.Repository;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.media.Json;
import io.vertx.core.Future;

import java.util.stream.Stream;

public sealed interface Domain {
  enum Namespace implements Domain {}

  interface Model<A extends Aggregate<S>, S extends Service> {
    A aggregate();
    S service();
  }

  interface State<S extends Enum<S>> {}
  interface Command {}
  interface Event {
    default EventInfo asInfo() {
      return new EventInfo(getClass().getSimpleName(), Json.with(this));
    }
  }

  interface Aggregates<A extends Aggregate<?>, I extends ID<?>> {
    static <A extends Domain.Aggregate<?>, I extends ID<?>> Aggregates<A, I> repository(EventStore eventStore, Domain.Hydration<A> hydration, String name) {
      return new Repository<>(eventStore, hydration, name);
    }

    Future<A> load(I id);
    Future<Void> save(AggregateInfo aggregate, Stream<EventInfo> events);
  }

  interface Aggregate<S extends Service> {
    S service(long version);
  }
  interface Hydration<A extends Aggregate<?>> {
    A aggregate(long version, Stream<EventInfo> events) throws Throwable;
  }
  interface Service {
    Future<Void> commit(ThrowableBiFunction<? super AggregateInfo, ? super Stream<EventInfo>, ? extends Future<Void>> transaction);
  }

  interface ID<R> extends Attribute<R> {}
}

