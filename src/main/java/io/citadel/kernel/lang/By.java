package io.citadel.kernel.lang;

import io.citadel.kernel.func.TryBiFunction;
import io.citadel.kernel.func.TryFunction;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;

public sealed interface By {
  enum Namespace implements By {}

  static <T, R> Collector<T, ?, R> reducing(R identity, TryBiFunction<? super R, ? super T, ? extends R> function) {
    return collectingAndThen(Collectors.reducing(TryFunction.<R>identity(), t -> r -> function.apply(r, t), TryFunction::then), finalizer -> finalizer.apply(identity));
  }
}
