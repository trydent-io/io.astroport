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

final class Fold<SOURCE, TRANSFORMED, PRETARGET, TARGET> implements Collector<SOURCE, PRETARGET, TARGET> {
  private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);

  private PRETARGET pretarget;

  private final TrySupplier<? extends PRETARGET> initializer;

  private final TryFunction<? super SOURCE, ? extends TRANSFORMED> transformer;
  private final TryBiFunction<? super PRETARGET, ? super TRANSFORMED, ? extends PRETARGET> accumulator;

  private final TryFunction<? super PRETARGET, ? extends TARGET> finisher;

  Fold(TrySupplier<? extends PRETARGET> initializer, TryFunction<? super SOURCE, ? extends TRANSFORMED> transformer, TryBiFunction<? super PRETARGET, ? super TRANSFORMED, ? extends PRETARGET> accumulator, TryFunction<? super PRETARGET, ? extends TARGET> finisher) {
    this.initializer = initializer;
    this.transformer = transformer;
    this.accumulator = accumulator;
    this.finisher = finisher;
  }

  @Override
  public Supplier<PRETARGET> supplier() {
    return () -> pretarget = initializer.get();
  }

  @Override
  public BiConsumer<PRETARGET, SOURCE> accumulator() {
    return (acc, elem) -> pretarget = accumulator.apply(pretarget, transformer.apply(elem));
  }

  @Override
  public BinaryOperator<PRETARGET> combiner() {
    return (acc1, acc2) -> acc1;
  }

  @Override
  public Function<PRETARGET, TARGET> finisher() {
    return folded -> finisher.apply(pretarget);
  }

  @Override
  public Set<Characteristics> characteristics() {
    return IdentityFinish;
  }
}
