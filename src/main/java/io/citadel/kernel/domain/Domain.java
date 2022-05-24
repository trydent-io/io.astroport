package io.citadel.kernel.domain;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.model.Defaults;
import io.citadel.kernel.domain.model.Service;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.func.ThrowableSupplier;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.Optional;
import java.util.stream.Stream;

public sealed interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {}

  interface State<S extends Enum<S> & State<S, E>, E extends Domain.Event> {
    @SuppressWarnings("unchecked")
    default boolean is(S... states) {
      var index = 0;
      while (index < states.length && states[index] != this)
        index++;
      return index < states.length;
    }
    Optional<S> push(E event);
  }

  interface Command {}

  interface Event {
    default Feed.Event asFeed() {return new Feed.Event(this.getClass().getSimpleName(), JsonObject.mapFrom(this));}
  }

  interface Archetype<M extends Record & Model<?>> {
    M generate(String id);
  }

  interface Timeline<S extends Enum<S> & State<S, ?>, T> {
    static <S extends Enum<S> & State<S, E>, E extends Domain.Event, M extends Record & Model<?>> Timeline<S, M> pastline(S initial, M archetype) {
      return new Type.Pastline<>(initial, archetype);
    }

    <F extends Domain.Event> Timeline<S, T> point(F event, ThrowableBiFunction<? super F, ? super T, ? extends T> then);
    default <F extends Domain.Event> Timeline<S, T> point(F event, ThrowableFunction<? super T, ? extends T> then) {
      return point(event, (it, t) -> then.apply(t));
    }

    <R> R freeze(ThrowableBiFunction<? super S, ? super T, ? extends R> then);
    default <R> R freeze(ThrowableFunction<? super T, ? extends R> then) {
      return freeze((s, t) -> then.apply(t));
    }

    enum Type {
      ;

      private static final class Pastline<S extends Enum<S> & State<S, E>, E extends Event, M extends Record & Model<?>> implements Timeline<S, M> {
        private final S state;
        private final M model;

        private Pastline(final S state, final M model) {
          this.state = state;
          this.model = model;
        }
        @SuppressWarnings("unchecked")
        @Override
        public <F extends Domain.Event> Timeline<S, M> point(final F event, final ThrowableBiFunction<? super F, ? super M, ? extends M> then) {
          return (Timeline<S, M>) state.push(event)
            .map(it -> new Pastline<>(it, then.apply(event, model)))
            .orElseThrow(() -> new IllegalStateException("Can't point event"));
        }

        @Override
        public <R> R freeze(final ThrowableBiFunction<? super S, ? super M, ? extends R> then) {
          return then.apply(state, model);
        }
      }

      private static final class Farline<S extends Enum<S> & State<S, E>, E extends Event> implements Timeline<S, Stream<E>> {
        private final S state;
        private final Stream<E> events;

        private Farline(final S state) {
          this(state, Stream.empty());
        }
        private Farline(final S state, final Stream<E> events) {
          this.state = state;
          this.events = events;
        }

        public <F extends E> Timeline<S, Stream<E>> point(final F event, final ThrowableBiFunction<? super F, ? super Stream<E>, ? extends Stream<E>> then) {
          return state.push(event)
            .map(it -> new Farline<>(state, then.apply(event, events)))
            .orElseThrow(() -> new IllegalStateException("Can't point event"));
        }

        @Override
        public <R> R freeze(final ThrowableBiFunction<? super S, ? super Stream<E>, ? extends R> then) {
          return then.apply(state, events);
        }
      }
    }
  }

  interface Snapshot<T extends Transaction<?, ?>> {
    Snapshot<T> archetype(String aggregateId, long aggregateVersion);
    Snapshot<T> hydrate(String eventName, JsonObject eventData);
    T transaction(EventStore eventStore);
  }

  interface Model<ID extends Domain.ID<?>> {
    ID id();
  }

  interface Lookup<T extends Transaction<?, ?>> {
    default Future<T> openAggregate(Domain.ID<?> id) {
      return openAggregate(id, null);
    }
    Future<T> openAggregate(ID<?> id, String name);
  }

  interface Transaction<M extends Record & Model<?>, E extends Domain.Event> {
    Transaction<M, E> has(ThrowablePredicate<? super M> condition);
    Transaction<M, E> log(ThrowableFunction<? super M, ? extends E> event);
    Transaction<M, E> log(ThrowableSupplier<? extends E> event);
    <R extends Transaction<?, ?>> R let(ThrowableFunction<? super M, ? extends R> aggregate);
    default Future<Void> commit() {
      return commit(null);
    }
    Future<Void> commit(String by);
  }

  interface ID<T> extends Attribute<T> {}
}

