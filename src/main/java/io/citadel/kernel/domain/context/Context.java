package io.citadel.kernel.domain.context;

import io.citadel.kernel.eventstore.EventPool;
import io.citadel.kernel.eventstore.metadata.Change;
import io.citadel.kernel.func.ThrowableBiConsumer;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowablePredicate;

import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public sealed interface Context<A> {

  @SuppressWarnings("unchecked")
  static <ID, A, E> Context<ID, A, E> empty() { return (Context<ID, A, E>) Empty.Default; }
  static <ID, A, E> Context<ID, A, E> transactional()

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
      case Transactional<ID, A, E> it -> it.context().flatMap(mapper);
      default -> Context.empty();
    };
  }

  default Context<ID, A, E> filter(ThrowablePredicate<? super A> predicate) {
    return switch (this) {
      case Bounded<ID, A, E> it && predicate.test(it.aggregate()) -> it;
      case Transactional<ID, A, E> it -> it.context().filter(predicate);
      default -> Context.empty();
    };
  }

  default Context<ID, A, E> peek(ThrowableBiConsumer<? super ID, ? super A> peeker) {
    switch (this) {
      case Bounded<ID, A, E> it -> peeker.accept(it.id(), it.aggregate());
      case Transactional<ID, A, E> it -> it.context().peek(peeker);
      case default -> {}
    }
    return this;
  }

  default Context<ID, A, E> log(ThrowableBiFunction<? super ID, ? super A, ? extends E> eventuate) {
    return switch (this) {
      case Bounded<ID, A, E> it -> eventuate.apply(it.id(), it.aggregate());
      case Transactional<ID, A, E> it -> new Transactional<>(it.context(), );
    }
  }
}

enum Empty implements Context<Object, Object, Object> { Default }
record Bounded<ID, A, E>(ID id, A aggregate) implements Context<ID, A, E> {}
record Transactional<ID, A, E>(Context<ID, A, E> context, Stream<Change> changes, EventPool pool) implements Context<ID, A, E> {
  Future<Void> commit() {
    return pool.update();
  }
}
