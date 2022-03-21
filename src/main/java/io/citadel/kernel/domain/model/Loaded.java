package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowableSupplier;
import io.vertx.core.Future;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class Loaded<I extends Domain.ID<?>, A extends Domain.Aggregate<A, S, M, E>, S extends Domain.State<?>, M extends Record, E extends Domain.Event> implements Domain.Aggregate<A, S, M, E> {
  private final I id;
  private final String name;
  private final long version;
  private final S state;
  private final M model;
  private final ThrowableFunction<? super Domain.Aggregate<A, S, M, E>, ? extends A> newAggregate;

  public Loaded(final I id, final String name, final long version, final S state, final M model, final ThrowableFunction<? super Domain.Aggregate<A, S, M, E>, ? extends A> newAggregate) {
    this.id = id;
    this.name = name;
    this.version = version;
    this.state = state;
    this.model = model;
    this.newAggregate = newAggregate;
  }

  private A throwIllegalState(String message) { throw new IllegalStateException(message); }

  @Override
  public A whenDefault(final S next, final ThrowableSupplier<M> modelling) {
    return isNull(state)
      ? newAggregate.apply(new Loaded<>(id, name, version, next, modelling.get(), newAggregate))
      : throwIllegalState("Can't move to next state, current aggregate state is not on default");
  }

  @Override
  public A when(final S state, final S next, final UnaryOperator<M> modelling) {
    return nonNull(state) && this.state.equals(state)
      ? newAggregate.apply(new Loaded<>(id, name, version, next, modelling.apply(model), newAggregate))
      : throwIllegalState("Can't move to next state, current aggregate state is not on %s".formatted(next));
  }

  @Override
  public Future<A> commit(final Domain.Transaction<A, ?, E> transaction) {
    return transaction.apply(id, name, version, Stream.empty());
  }
}

final class Ready<I extends Domain.ID<?>, A extends Domain.Aggregate<A, S, M, E>, S extends Domain.State<?>, M extends Record, E extends Domain.Event> implements Domain.Aggregate<A, S, M, E> {
  private final Domain.Aggregate<A, S, M, E> aggregate;
  private final

  @Override
  public A whenDefault(final S next, final ThrowableSupplier<M> modelling) {
    return null;
  }

  @Override
  public A when(final S state, final S next, final UnaryOperator<M> modelling) {
    return null;
  }

  @Override
  public A open() {
    return null;
  }

  @Override
  public <T> T close(final ThrowableFunction<? super Stream<E>, ? extends T> callback) {
    return null;
  }
}
