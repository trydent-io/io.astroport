package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.State;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;

public final class Context<R extends Record, S extends Enum<S> & State<S, E>, E> {
  private final R model;
  private final S state;
  private final Transaction transaction;

  Context(R model, S state, Transaction transaction) {
    this.model = model;
    this.state = state;
    this.transaction = transaction;
  }

  public Context<R, S, E> has(ThrowablePredicate<? super R> condition) {
    return condition.test(model) ? this : null;
  }

  public <T> T load(ThrowableFunction<? super R, ? extends T> loader) {
    return loader.apply(model);
  }

}
