package io.citadel.kernel.domain;

import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowableSupplier;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface Stateful<S extends Domain.State<?>, T> {
  static <S extends Domain.State<?>, T> Stateful<S, T> ofDefault() {
    return new Type.Staging<>();
  }
  Stateful<S, T> whenDefault(S next, ThrowableSupplier<T> then);
  Stateful<S, T> when(S state, S next, ThrowableFunction<T, T> then);

  enum Type {;
    static final class Staging<S extends Domain.State<?>, T> implements Stateful<S, T> {
      private final S state;
      private final ThrowableSupplier<T> initializator;
      private final ThrowableFunction<T, T> operator;

      Staging() { this(null, null, null); }
      Staging(S state, ThrowableSupplier<T> initializator, ThrowableFunction<T, T> operator) {
        this.state = state;
        this.initializator = initializator;
        this.operator = operator;
      }

      @Override
      public Stateful<S, T> whenDefault(S next, ThrowableSupplier<T> then) {
        return isNull(state) ? new Staging<>(next, then, null) : this;
      }

      @Override
      public Stateful<S, T> when(S state, S next, ThrowableFunction<T, T> then) {
        return nonNull(this.state) && this.state.equals(state) ? new Staging<>(next, initializator, then) : this;
      }
    }
  }
}
