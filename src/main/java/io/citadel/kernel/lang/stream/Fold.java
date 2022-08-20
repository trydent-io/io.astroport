package io.citadel.kernel.lang.stream;

import io.citadel.kernel.func.TryBiFunction;
import io.citadel.kernel.func.TryFunction;
import io.citadel.kernel.func.TrySupplier;
import io.citadel.kernel.func.TryTriFunction;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
final class Fold<STATE, SOURCE, TRANSFORMED, PRETARGET, TARGET> implements Collector<SOURCE, PRETARGET, TARGET> {
  record Folded<STATE, SOURCE, PRETARGET>(STATE state, SOURCE source, PRETARGET pretarget) {
  }

  private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);

  private final Object lock = new Object();
  private volatile STATE state;
  private volatile PRETARGET pretarget;

  private final TrySupplier<? extends STATE> initial;
  private final TrySupplier<? extends PRETARGET> initializer;

  private final TryFunction<? super SOURCE, ? extends TRANSFORMED> transformer;
  private final TryTriFunction<? super STATE, ? super PRETARGET, ? super TRANSFORMED, ? extends PRETARGET> accumulator;

  private final TryBiFunction<? super SOURCE, ? super PRETARGET, ? extends TARGET> finisher;

  Fold(TrySupplier<? extends STATE> initial, TrySupplier<? extends PRETARGET> initializer, TryFunction<? super SOURCE, ? extends TRANSFORMED> transformer, TryTriFunction<? super STATE, ? super PRETARGET, ? super TRANSFORMED, ? extends PRETARGET> accumulator, TryBiFunction<? super SOURCE, ? super PRETARGET, ? extends TARGET> finisher) {
    this.initial = initial;
    this.initializer = initializer;
    this.transformer = transformer;
    this.accumulator = accumulator;
    this.finisher = finisher;
  }

  @Override
  public Supplier<PRETARGET> supplier() {
    return () -> {
      if (pretarget == null && state == null) {
        synchronized (lock) {
          if (pretarget == null && state == null) {
            pretarget = initializer.get();
            state = initial.get();
          }
        }
      }
      return pretarget;
    };
  }

  @Override
  public BiConsumer<PRETARGET, SOURCE> accumulator() {
    return (acc, elem) -> {
      synchronized (lock) {
        final var transformed = transformer.apply(elem);
        pretarget = accumulator.apply(acc.pretarget, transformer.apply(elem)));
      }
    };
  }

  @Override
  public BinaryOperator<Folded<SOURCE, PRETARGET>> combiner() {
    return (acc1, acc2) -> acc1;
  }

  @Override
  public Function<Folded<SOURCE, PRETARGET>, TARGET> finisher() {
    return folded -> finisher.apply(folded.source, folded.pretarget);
  }

  @Override
  public Set<Characteristics> characteristics() {
    return IdentityFinish;
  }
}
