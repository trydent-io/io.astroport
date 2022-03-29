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

  interface Hydration<A extends Aggregate<A>> {
    A aggregate(long version, Stream<MetaEvent> events) throws Throwable;
  }
  interface Transaction<A extends Aggregate<A>> {
    Future<Void> commit(ThrowableBiFunction<? super MetaAggregate, ? super Stream<MetaEvent>, ? extends Future<Void>> commit);
  }

  interface ValueObject<R extends Record & ValueObject<R>> {
    default boolean is(R valueObject) { return this.equals(valueObject); }
  }
  interface ID<R extends Record & ID<R>> extends ValueObject<R> {}

  interface Model {
    static <M> Identity<M> identity(Domain.ID<?> id, long version, Domain.State<?> initial) {
      return new Identity.Root<>(id, version, initial);
    }

    interface Identity<M> extends Model {
      Next<M> nextIf(Domain.State<?> state, Domain.State<?> next, ThrowableSupplier<M> modelling);

      Next<M> stayIf(Stream<Domain.State<?>> states, ThrowableSupplier<M> modelling);

      record Root<M>(Domain.ID<?> id, long version, Domain.State<?> initial) implements Identity<M> {
        @Override
        public Next<M> nextIf(final Domain.State<?> state, final Domain.State<?> next, final ThrowableSupplier<M> modelling) {
          return this.initial.equals(state)
            ? new Next.Modelled<>(new Root<>(id, version, next), modelling.get())
            : throwIllegalState();
        }

        @Override
        public Next<M> stayIf(final Stream<Domain.State<?>> states, final ThrowableSupplier<M> modelling) {
          return states.anyMatch(initial::equals)
            ? new Next.Modelled<>(new Root<>(id, version, initial), modelling.get())
            : throwIllegalState();
        }

        private Next<M> throwIllegalState() {
          throw new IllegalStateException("Can't to do something");
        }
      }
    }

    interface Next<M> extends Model {
      Next<M> nextIf(Domain.State<?> state, Domain.State<?> next, UnaryOperator<M> modelling);

      Next<M> stayIf(Stream<Domain.State<?>> states, UnaryOperator<M> modelling);

      record Modelled<M>(Identity<M> identity, M model) implements Next<M> {
        @Override
        public Next<M> nextIf(final Domain.State<?> state, final Domain.State<?> next, final UnaryOperator<M> modelling) {
          return this.identity.nextIf(state, next, () -> modelling.apply(model));
        }

        @Override
        public Next<M> stayIf(final Stream<Domain.State<?>> states, final UnaryOperator<M> modelling) {
          return this.identity.stayIf(states, () -> modelling.apply(model));
        }
      }
    }
  }

  interface Snapshot<A extends Domain.Aggregate<A>> {
    A freeze(long version);
  }
}

