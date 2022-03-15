package io.citadel.shared.context;

import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.shared.context.attribute.Attribute;
import io.citadel.shared.context.attribute.LongAttribute;
import io.citadel.shared.context.attribute.Serial;
import io.citadel.shared.context.repository.Repository;
import io.citadel.shared.context.repository.Root;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.lang.Long.MAX_VALUE;

public sealed interface Domain {
  enum Namespace implements Domain {}

  interface State<E extends Enum<E>> {}
  interface Command {}
  interface Event {
    default String $name() { return this.getClass().getSimpleName(); }
    default JsonObject $asJson() { return JsonObject.mapFrom(this); }
  }
  interface Model {}

  interface Aggregate<A extends Aggregate<A, M, S>, M extends Record & Model, S extends State<?>> {
    default boolean is(S state) {
      return switch (this) {
        case Root<?, ?, ?, S> root -> root.state().equals(state);
        default -> false;
      };
    }

    Aggregate<A, M, S> nextIf(S state, S next, UnaryOperator<M> model);
  }
  interface Aggregates<A extends Aggregate<?, ?, ?>, I extends ID<?>, E extends Domain.Event> {
    Future<A> load(I id);
    Future<Void> save(I id, long version, Stream<E> events);

    static <A extends Aggregate<?, ?, ?>, I extends ID<?>, E extends Domain.Event> Aggregates<A, I, E> repository(EventStore eventStore, Domain.Hydration<A> hydration, String name) {
      return new Repository<>(eventStore, hydration, name);
    }
  }

  interface Hydration<A extends Aggregate<?>> {
    A apply(long version, Stream<EventInfo> events) throws Throwable;
  }

  interface Version extends LongAttribute {
    static Domain.Version first() {return Versions.Defaults.First;}
    static Domain.Version last() {return Versions.Defaults.Last;}
    static Optional<Version> of(long value) {
      return Optional.of(value).filter(it -> it >= 0).map(Serial::new);
    }
  }
  interface ID<T> extends Attribute<T> {}
}

enum Versions {;
  enum Defaults implements Domain.Version {
    First {
      @Override
      public long value() {
        return 0;
      }
    },
    Last {
      @Override
      public long value() {
        return MAX_VALUE;
      }
    }
  }
}
