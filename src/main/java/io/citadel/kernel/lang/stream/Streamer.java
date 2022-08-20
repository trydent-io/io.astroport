package io.citadel.kernel.lang.stream;

import io.citadel.kernel.func.TryBiFunction;
import io.citadel.kernel.func.TryFunction;
import io.citadel.kernel.func.TrySupplier;
import io.citadel.kernel.lang.stream.Fold.Folded;

import java.util.stream.Collector;
import java.util.stream.Stream;

public interface Streamer {
  @SuppressWarnings("unchecked")
  default <T> Stream<T> append(Stream<T> origin, T... items) {
    return Stream.concat(origin, Stream.of(items));
  }

  default <SOURCE, TRANSFORMED, PRETARGET, TARGET> Fold<SOURCE, TRANSFORMED, PRETARGET, TARGET> folding(TrySupplier<? extends PRETARGET> initializer, TryFunction<? super SOURCE, ? extends TRANSFORMED> transformer, TryBiFunction<? super PRETARGET, ? super TRANSFORMED, ? extends PRETARGET> accumulator, TryBiFunction<? super SOURCE, ? super PRETARGET, ? extends TARGET> finisher) {
    return new Fold<>(initializer, transformer, accumulator, finisher);
  }
}
