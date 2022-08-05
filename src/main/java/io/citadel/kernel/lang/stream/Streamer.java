package io.citadel.kernel.lang.stream;

import io.citadel.kernel.func.TryBiFunction;
import io.citadel.kernel.func.TryFunction;
import io.citadel.kernel.func.TrySupplier;

import java.util.stream.Collector;
import java.util.stream.Stream;

public interface Streamer {
  @SuppressWarnings("unchecked")
  default <T> Stream<T> append(Stream<T> origin, T... items) {
    return Stream.concat(origin, Stream.of(items));
  }

  default <SOURCE, TARGET> Collector<SOURCE, TARGET, TARGET> folding(TrySupplier<? extends TARGET> initializer, TryBiFunction<? super TARGET, ? super SOURCE, ? extends TARGET> accumulator) {
    return Fold.of(initializer, accumulator);
  }

  default <MAPPED, SOURCE, TARGET> Collector<SOURCE, TARGET, TARGET> folding(TrySupplier<? extends TARGET> initializer, TryFunction<? super SOURCE, ? extends MAPPED> mapper, TryBiFunction<? super TARGET, ? super MAPPED, ? extends TARGET> accumulator) {
    return Fold.of(initializer, mapper, accumulator);
  }
}
