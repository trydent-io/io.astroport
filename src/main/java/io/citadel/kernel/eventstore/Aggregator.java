package io.citadel.kernel.eventstore;

import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public interface Aggregator<T, A, R> extends Collector<T, A, R> {
  @Override
  default Set<Characteristics> characteristics() { return Default.Companion.IdentityFinish; }

  @Override
  default BinaryOperator<A> combiner() {
    return (a, b) -> a;
  }

  enum Default {
    Companion;
    private final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);
  }
}
