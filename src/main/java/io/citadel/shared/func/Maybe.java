package io.citadel.shared.func;

import java.util.function.BiFunction;

@SuppressWarnings({"unchecked"})
public sealed interface Maybe<T> {
  static <T> Maybe<T> empty() {
    return (Maybe<T>) Type.Empty.Default;
  }

  static <T> Maybe<T> of(T value) {
    return value == null ? empty() : new Type.Right<>(value);
  }

  static <T> Maybe<T> failure(Throwable throwable) {
    return new Type.Left<>(throwable);
  }

  default  <R> Maybe<R> map(ThrowableFunction<? super T, ? extends R> function) {
    return switch (this) {
      case Type.Empty ignored -> empty();
      case Type.Right<T> right -> (Maybe<R>) function.apply(right.value);
      case Type.Left<T> left -> new Type.Left<>(left.throwable);
    };
  }

  default  <R> Maybe<R> flatMap(ThrowableFunction<? super T, ? extends Maybe<? extends R>> function) {
    return switch (this) {
      case Type.Empty ignored -> empty();
      case Type.Right<T> right -> function.apply(right.value).flatMap(it -> it);
      case Type.Left<T> left -> new Type.Left<>(left.throwable);
    };
  }

  default Maybe<T> filter(ThrowablePredicate<? super T> predicate) {
    return switch (this) {
      case Type.Right<T> right -> predicate.test(right.value) ? this : empty();
      default -> this;
    };
  }

  default Maybe<T> peek(ThrowableConsumer<? super T> consumer) {
    return switch (this) {
      case Type.Right<T> right -> consumer.accept(this, right.value);
      default -> this;
    };
  }

  default T otherwise(String message, BiFunction<? super String, ? super Throwable, ? extends RuntimeException> function) {
    return switch (this) {
      case Type.Empty ignored -> throw function.apply(message, new IllegalStateException("Can't retrieve value, state is empty"));
      case Type.Right<T> right -> right.value;
      case Type.Left<T> left -> throw function.apply(message, left.throwable);
    };
  }

  default Maybe<T> or(ThrowableSupplier<? extends T> supplier) {
    return switch (this) {
      case Type.Right<T> right -> this;
      default -> supplier.get().map(it -> it);
    };
  }

  default T otherwise(ThrowableSupplier<? extends T> supplier) {
    return switch (this) {
      case Type.Right<T> right -> right.value;
      default -> supplier.get().otherwise("Can't retrieve value", IllegalStateException::new);
    };
  }

  default T or(T value) {
    return switch (this) {
      case Type.Right<T> right -> right.value;
      default -> value;
    };
  }

  default Maybe<T> exceptionally(ThrowableConsumer<? super Throwable> consumer) {
    return switch (this) {
      case Type.Left<T> left -> consumer.accept(this, left.throwable);
      default -> this;
    };
  }

  enum Type {;
    private enum Empty implements Maybe<Object> {Default}
    private record Left<T>(Throwable throwable) implements Maybe<T> {}
    private record Right<T>(T value) implements Maybe<T> {}
  }
}
