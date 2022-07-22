package io.citadel.kernel.func;

import io.citadel.kernel.func.Maybe.Type.Empty;
import io.citadel.kernel.func.Maybe.Type.Left;
import io.citadel.kernel.func.Maybe.Type.Right;

import java.util.function.BiFunction;

@SuppressWarnings({"unchecked"})
public sealed interface Maybe<T> {
  static <T> Maybe<T> empty() {
    return (Maybe<T>) Empty.Default;
  }

  static <T> Maybe<T> of(T value) {
    return value == null ? empty() : new Right<>(value);
  }

  static <T> Maybe<T> error(Throwable throwable) {
    return new Left<>(throwable);
  }

  default <R> Maybe<R> map(ThrowableFunction<? super T, ? extends R> function) {
    return switch (this) {
      case Empty ignored -> empty();
      case Right<T> right -> new Right<>(function.apply(right.value));
      case Left<T> left -> new Left<>(left.throwable);
    };
  }

  default <R> Maybe<R> map(TrySupplier<? extends R> supplier) {
    return switch (this) {
      case Empty ignored -> empty();
      case Right<T> ignored -> new Right<>(supplier.get());
      case Left<T> left -> new Left<>(left.throwable);
    };
  }

  default <R> Maybe<R> flatMap(ThrowableFunction<? super T, ? extends Maybe<? extends R>> function) {
    return switch (this) {
      case Empty ignored -> empty();
      case Right<T> right -> (Maybe<R>) function.apply(right.value);
      case Left<T> left -> new Left<>(left.throwable);
    };
  }

  default Maybe<T> filter(ThrowablePredicate<? super T> predicate) {
    return switch (this) {
      case Right<T> right -> predicate.test(right.value) ? this : empty();
      default -> this;
    };
  }

  default Maybe<T> peek(ThrowableConsumer<? super T> consumer) {
    return switch (this) {
      case Right<T> right -> consumer.accept(this, right.value);
      default -> this;
    };
  }

  default Maybe<T> or(String message, BiFunction<? super String, ? super Throwable, ? extends RuntimeException> function) {
    return switch (this) {
      case Empty ignored -> Maybe.error(function.apply(message, new IllegalStateException("Can't retrieve value, lifecycle is empty")));
      case Left<T> left -> Maybe.error(function.apply(message, left.throwable));
      default -> this;
    };
  }

  default T otherwise(String message, BiFunction<? super String, ? super Throwable, ? extends RuntimeException> function) {
    return switch (this) {
      case Empty ignored -> throw function.apply(message, new IllegalStateException("Can't retrieve value, lifecycle is empty"));
      case Left<T> left -> throw function.apply(message, left.throwable);
      case Right<T> right -> right.value;
    };
  }

  default Maybe<T> or(TrySupplier<? extends T> supplier) {
    return switch (this) {
      case Right<T> ignored -> this;
      default -> Maybe.of(supplier.get());
    };
  }

  default T otherwise(TrySupplier<? extends T> supplier) {
    return switch (this) {
      case Right<T> right -> right.value;
      default -> supplier.get();
    };
  }

  default Maybe<T> or(T value) {
    return switch (this) {
      case Right<T> right -> this;
      default -> Maybe.of(value);
    };
  }

  default T otherwise(T value) {
    return switch (this) {
      case Right<T> right -> right.value;
      default -> value;
    };
  }

  default Maybe<T> exceptionally(ThrowableConsumer<? super Throwable> consumer) {
    return switch (this) {
      case Left<T> left -> consumer.accept(this, left.throwable);
      default -> this;
    };
  }

  enum Type {
    ;

    enum Empty implements Maybe<Object> {Default}
    record Left<T>(Throwable throwable) implements Maybe<T> {}
    record Right<T>(T value) implements Maybe<T> {}
  }
}
