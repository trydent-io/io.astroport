package io.citadel.kernel.lang.stream;

import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableSupplier;

import java.util.stream.Collector;
import java.util.stream.Stream;

public interface Streamer<T> {
  @SuppressWarnings("unchecked")
  default Stream<T> append(Stream<T> origin, T... items) {
    return Stream.concat(origin, Stream.of(items));
  }

  default <R> Collector<T, R, R> folding(ThrowableSupplier<? extends R> initializer, ThrowableBiFunction<? super R, ? super T, ? extends R> accumulator) {
    return Fold.of(initializer, accumulator);
  }
}
