package io.citadel.kernel.domain.model;

import io.citadel.CitadelException;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.Future;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class Default<A extends Domain.Aggregate<A>, S extends Domain.State<?>, M extends Record> implements Domain.Aggregate<A> {
  private final ThrowableFunction<? super Domain.Aggregate<A>, ? extends A> function;

  public Default(final ThrowableFunction<? super Domain.Aggregate<A>, ? extends A> function) {this.function = function;}

  @Override
  public Optional<A> whenDefault(final S next, final Supplier<M> supplier) {
    return Optional.of(function.apply(new Next<>(function, next, supplier.get())));
  }

  @Override
  public Optional<A> when(final S state, final S next, final UnaryOperator<M> operator) {
    return Optional.empty();
  }

  @Override
  public Future<A> commit(final Domain.Transaction<A> transaction) {
    return Future.failedFuture(new CitadelException("Can't commit transaction, aggregate state is on default"));
  }
}

final class Next<A extends Domain.Aggregate<A, S, M>, S extends Domain.State<?>, M extends Record> implements Domain.Aggregate<A, S, M> {
  private final ThrowableFunction<? super Domain.Aggregate<A, S, M>, ? extends A> function;
  private final S state;
  private final M model;

  Next(final ThrowableFunction<? super Domain.Aggregate<A, S, M>, ? extends A> function, final S state, final M model) {
    this.function = function;
    this.state = state;
    this.model = model;
  }

  @Override
  public Optional<A> whenDefault(final S next, final Supplier<M> supplier) {
    return Optional.empty();
  }

  @Override
  public Optional<A> when(final S state, final S next, final UnaryOperator<M> operator) {
    return Optional.of(this.state)
      .filter(it -> it.equals(state))
      .map(it -> operator.apply(model))
      .map(it -> new Next<>(function, next, it))
      .map(function);
  }

  @Override
  public Future<A> commit(final Domain.Transaction<A> transaction) {
    return Future.failedFuture(new CitadelException("Can't commit transaction, aggregate state is not committable yet"));
  }
}

