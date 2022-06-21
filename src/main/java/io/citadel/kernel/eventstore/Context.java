package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.State;
import io.citadel.kernel.eventstore.meta.Event;
import io.citadel.kernel.media.Json;

import java.util.function.Function;
import java.util.function.Predicate;

sealed interface Context<R, E> {
  @SuppressWarnings("unchecked")
  static <R, E> Context<R, E> empty() { return (Context<R, E>) Default.Empty;}

  static <R, E, S extends Enum<S> & State<S, E>> Context<R, E> of(R entity, S state, Transaction transaction) {
    return new Invariant<>(entity, state, transaction);
  }

  default Context<R, E> has(Predicate<? super R> predicate) { return this; }
  default Context<R, E> log(Function<? super R, ? extends E> event) { return this; }
}

enum Default implements Context<Object, Object> {Empty}
final class Invariant<R, E, S extends Enum<S> & State<S, E>> implements Context<R, E> {
  private final R entity;
  private final S state;
  private final Transaction transaction;

  Invariant(R entity, S state, Transaction transaction) {
    this.entity = entity;
    this.state = state;
    this.transaction = transaction;
  }

  @Override
  public Context<R, E> has(Predicate<? super R> predicate) {
    return predicate.test(entity) ? this : Context.empty();
  }

  @Override
  public Context<R, E> log(Function<? super R, ? extends E> eventually) {
    final var event = eventually.apply(entity);
    if (state.transitable(event)) transaction.log(Event.of(event.getClass().getSimpleName(), Json.fromAny(event)));
    return this;
  }
}
