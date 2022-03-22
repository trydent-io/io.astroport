package io.citadel.kernel.domain.model;

import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableSupplier;
import io.vertx.core.Future;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

final class Meta<I extends Domain.ID<?>, A extends Domain.Aggregate<A>> implements Domain.Aggregate<A> {
  private final I id;
  private final String name;
  private final long version;

  Meta(final I id, final String name, final long version) {
    this.id = id;
    this.name = name;
    this.version = version;
  }

  @Override
  public Future<A> commit(final Domain.Transaction<A> transaction) {
    return transaction.apply(new AggregateInfo(id.value().toString(), name, version), Stream.empty());
  }
}

final class Modelled<S extends Domain.State<?>, M extends Record, A extends Domain.Aggregate<A>> implements Domain.Aggregate<A> {
  private final A aggregate;
  private final S state;
  private final M model;

  Modelled(final A aggregate, final S state, final M model) {
    this.aggregate = aggregate;
    this.state = state;
    this.model = model;
  }

  @Override
  public Future<A> commit(final Domain.Transaction<A> transaction) {
    return aggregate.commit(transaction);
  }
}

final class Committed<E extends Domain.Event, A extends Domain.Aggregate<A>> implements Domain.Aggregate<A> {
  private final A aggregate;
  private final Stream<E> events;

  Committed(final A aggregate, final Stream<E> events) {
    this.aggregate = aggregate;
    this.events = events;
  }

  @Override
  public Future<A> commit(final Domain.Transaction<A> transaction) {
    return aggregate.commit((aggregate, events) -> transaction.apply(aggregate, this.events.map(it -> new EventInfo(it.$name(), it.$asJson()))));
  }
}

public final class Loaded<I extends Domain.ID<?>, A extends Domain.Aggregate<A, S, M, E>, S extends Domain.State<?>, M extends Record, E extends Domain.Event> implements Domain.Aggregate<A, S, M, E> {
  private final I id;
  private final String name;
  private final long version;
  private final S state;
  private final M model;
  private final Stream<E> events;
  private final ThrowableFunction<? super Domain.Aggregate<A, S, M, E>, ? extends A> newAggregate;

  public Loaded(final I id, final String name, final long version, final S state, final M model, final Stream<E> events, final ThrowableFunction<? super Domain.Aggregate<A, S, M, E>, ? extends A> newAggregate) {
    this.id = id;
    this.name = name;
    this.version = version;
    this.state = state;
    this.model = model;
    this.events = events;
    this.newAggregate = newAggregate;
  }

  private A throwIllegalState(String message) { throw new IllegalStateException(message); }

  @Override
  public A whenDefault(final S next, final ThrowableSupplier<M> modelling) {
    return isNull(state)
      ? newAggregate.apply(new Loaded<>(id, name, version, next, modelling.get(), events, newAggregate))
      : throwIllegalState("Can't move to next state, current aggregate state is not on default");
  }

  @Override
  public A when(final S state, final S next, final UnaryOperator<M> modelling) {
    return nonNull(state) && this.state.equals(state)
      ? newAggregate.apply(new Loaded<>(id, name, version, next, modelling.apply(model), events, newAggregate))
      : throwIllegalState("Can't move to next state, current aggregate state is not on %s".formatted(next));
  }

  @Override
  public Future<A> commit(final Domain.Transaction<A> transaction) {
    return transaction.apply(new AggregateInfo(id.value().toString(), name, version), Stream.empty());
  }
}

