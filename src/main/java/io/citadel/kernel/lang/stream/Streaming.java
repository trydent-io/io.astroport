package io.citadel.kernel.lang.stream;

import io.citadel.kernel.func.TryBiFunction;
import io.citadel.kernel.func.TryFunction;
import io.citadel.kernel.func.TrySupplier;

import java.util.stream.Collector;
import java.util.stream.Stream;

public interface Streaming {
  @SuppressWarnings("unchecked")
  default <T> Stream<T> append(Stream<T> origin, T... items) {
    return Stream.concat(origin, Stream.of(items));
  }

  default <SOURCE, TRANSFORMED, PRETARGET, TARGET> Collector<SOURCE, PRETARGET, TARGET> folding(
    TrySupplier<PRETARGET> initializer,
    TryFunction<? super SOURCE, ? extends TRANSFORMED> transformer,
    TryBiFunction<? super PRETARGET, ? super TRANSFORMED, ? extends PRETARGET> accumulator,
    TryFunction<? super PRETARGET, ? extends TARGET> finisher
    ) {
    return new Fold<>(initializer, transformer, accumulator, finisher);
  }

  default <SOURCE, TARGET> Collector<SOURCE, TARGET, TARGET> folding(
    TrySupplier<TARGET> initializer,
    TryBiFunction<? super TARGET, ? super SOURCE, ? extends TARGET> accumulator
  ) {
    return new Fold<>(initializer, TryFunction.identity(), accumulator, TryFunction.identity());
  }
}
