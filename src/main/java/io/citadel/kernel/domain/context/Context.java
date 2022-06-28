package io.citadel.kernel.domain.context;

import io.citadel.kernel.eventstore.EventStorePool;

import java.util.function.BiFunction;

public sealed interface Context<ID, A, E> {

  @SuppressWarnings("unchecked")
  static <ID, A, E> Context<ID, A, E> empty() { return (Context<ID, A, E>) Empty.Default; }

  default <R extends A> Context<ID, R, E> map(BiFunction<? super ID, ? super A, ? extends R> mapper) {
    return switch (this) {
      case Bounded<ID, A, E> it -> new Bounded<>(it.id(), mapper.apply(it.id(), it.aggregate()));
      case Transactional<ID, A, E> it -> new Transactional<>(it.map(mapper), it.pool());
      default -> Context.empty();
    };
  }

  default <I, B, F> Context<I, B, F> flatMap(BiFunction<? super ID, ? super A, ? extends Context<I, B, F>> mapper) {
    return switch (this) {
      case Bounded<ID, A, E> it -> mapper.apply(it.id(), it.aggregate());
      default -> Context.empty();
    };
  }
}

enum Empty implements Context<Object, Object, Object> { Default; }
record Bounded<ID, A, E>(ID id, A aggregate) implements Context<ID, A, E> {}

record Transactional<ID, A, E>(Context<ID, A, E> context, EventStorePool pool) implements Context<ID, A, E> {}
