package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;

public final class Context<M extends Record & Domain.Model<?>, S extends Enum<S> & Domain.State<S, E>, E extends Domain.Event> {
  private final M model;
  private final String name;
  private final long version;

  private final S state;
  private final Transaction<E> transaction;

  Context(M model, String name, long version, Transaction<E> transaction) {
    this.model = model;
    this.name = name;
    this.version = version;
    this.transaction = transaction;
  }

  public Context<M, E> has(ThrowablePredicate<? super M> condition) {
    return condition.test(model) ? this : null;
  }

  public <T> T load(ThrowableFunction<? super M, ? extends T> loader) {
    return loader.apply(model);
  }

  public Context<M, E> reload(ThrowableFunction<? super Domain.ID<?>, ? extends Context<M, E>> reloader) {
    return reloader.apply(model.id());
  }
}
