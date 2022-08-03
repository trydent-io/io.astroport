package io.citadel.kernel.lang;

import io.citadel.kernel.func.TryFunction;
import io.citadel.kernel.func.TryPredicate;

import java.util.Optional;

@SuppressWarnings("unchecked")
public interface Scope<T, S extends Scope<T, S>> {
  T value();

  default Optional<S> takeIf(TryPredicate<? super T> predicate) {
    return switch (this) {
      case Scope<T, S> it && predicate.test(it.value()) -> Optional.of((S) it);
      default -> Optional.empty();
    };
  }

  default Optional<S> takeUnless(TryPredicate<? super T> predicate) {
    return switch (this) {
      case Scope<T, S> it && !predicate.test(it.value()) -> Optional.of((S) it);
      default -> Optional.empty();
    };
  }

  default <L, R extends Scope<L, R>> Optional<R> let(TryFunction<? super T, ? extends R> function) {
    return switch (this) {
      case Scope<T, S> it && it.value() != null -> Optional.ofNullable(function.apply(it.value()));
      default -> Optional.empty();
    };
  }
}
