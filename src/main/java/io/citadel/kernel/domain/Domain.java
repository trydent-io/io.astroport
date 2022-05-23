package io.citadel.kernel.domain;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.model.Changes;
import io.citadel.kernel.domain.model.Defaults;
import io.citadel.kernel.domain.model.Service;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.func.ThrowableSupplier;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public sealed interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {}

  interface State<S extends Enum<S> & State<S>> {
    @SuppressWarnings("unchecked")
    default boolean is(S... states) {
      var index = 0;
      while (index < states.length && states[index] != this) index++;
      return index < states.length;
    }

    S next();
  }

  interface Command {}
  interface Event {
    default Feed.Event asFeed() { return new Feed.Event(this.getClass().getSimpleName(), JsonObject.mapFrom(this)); }
  }

  interface Archetype<M extends Record & Model<?>> {
    M generate(String id);
  }

  interface Timeline<E extends Domain.Event, R> extends Supplier<R> {
    Timeline<E, R> take(E event);
    Timeline<E, R> freeze();

    enum Type {;

      private static final class Past<S extends Enum<S> & State<S>, E extends Event, M extends Record & Model<?>> implements Timeline<E, M> {
        private final S state;
        private final M model;
        private final UnaryOperator<S> next;
        private final ThrowableBiFunction<? super S, ? super M, ? extends M> applier;

        private Past(S state, M model, UnaryOperator<S> next, ThrowableBiFunction<? super S, ? super M, ? extends M> applier) {
          this.state = state;
          this.model = model;
          this.next = next;
          this.applier = applier;
        }

        @Override
        public Timeline<E, M> take(E event) {
          return new Past<>(next.apply(state), applier.apply(state, model), next, applier);
        }

        @Override
        public M get() {
          return null;
        }
      }

    }
  }

  interface Snapshot<A extends Aggregate<?, ?>>  {
    Snapshot<A> archetype(String aggregateId, long aggregateVersion);
    Snapshot<A> hydrate(String eventName, JsonObject eventData);
    A transaction(EventStore eventStore);
  }

  interface Model<ID extends Domain.ID<?>> {
    ID id();
  }

  interface Lookup<A extends Aggregate<?, ?>> {
    default Future<A> findAggregate(Domain.ID<?> id) {
      return findAggregate(id, null);
    }
    Future<A> findAggregate(ID<?> id, String name);
  }

  interface Aggregate<M extends Record & Model<?>, E extends Domain.Event> {
    Aggregate<M, E> asserts(ThrowablePredicate<? super M> where);
    Aggregate<M, E> notify(ThrowableFunction<? super M, ? extends E> event);
    Aggregate<M, E> notify(ThrowableSupplier<? extends E> event);
    <R extends Aggregate<?, ?>> R supply(ThrowableFunction<? super M, ? extends R> aggregate);

    default Future<Void> submit() {
      return submit(null);
    }
    Future<Void> submit(String by);
  }

  sealed interface Transaction<E extends Domain.Event> permits Changes {
    Transaction<E> log(E event);

    Future<Void> commit(String aggregateId, String aggregateName, long aggregateVersion, String by);

    default Future<Void> commit(String aggregateId, String aggregateName, long aggregateVersion) {
      return commit(aggregateId, aggregateName, aggregateVersion, null);
    }
  }

  interface ID<T> extends Attribute<T> {}
}

