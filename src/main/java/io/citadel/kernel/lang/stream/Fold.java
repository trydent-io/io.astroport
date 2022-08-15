package io.citadel.kernel.lang.stream;

import io.citadel.kernel.func.TryBiFunction;
import io.citadel.kernel.func.TryFunction;
import io.citadel.kernel.func.TrySupplier;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
final class Fold<SOURCE, TRANSFORMED, PRETARGET, TARGET> implements Collector<SOURCE, Fold.Folded<SOURCE, PRETARGET>, TARGET> {
  record Folded<SOURCE, PRETARGET>(SOURCE source, PRETARGET pretarget) {
  }

  private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);

  private final Object lock = new Object();
  private volatile Folded<SOURCE, PRETARGET> reference;
  private final TrySupplier<? extends PRETARGET> initializer;

  private final TryFunction<? super SOURCE, ? extends TRANSFORMED> transformer;
  private final TryBiFunction<? super PRETARGET, ? super TRANSFORMED, ? extends PRETARGET> accumulator;

  private final TryBiFunction<? super SOURCE, ? super PRETARGET, ? extends TARGET> finisher;

  Fold(TrySupplier<? extends PRETARGET> initializer, TryFunction<? super SOURCE, ? extends TRANSFORMED> transformer, TryBiFunction<? super PRETARGET, ? super TRANSFORMED, ? extends PRETARGET> accumulator, TryBiFunction<? super SOURCE, ? super PRETARGET, ? extends TARGET> finisher) {
    this.initializer = initializer;
    this.transformer = transformer;
    this.accumulator = accumulator;
    this.finisher = finisher;
  }

  static <SOURCE, TARGET> Fold<SOURCE, SOURCE, TARGET, TARGET> of(TrySupplier<? extends TARGET> initializer, TryBiFunction<? super TARGET, ? super SOURCE, ? extends TARGET> folder) {
    return new Fold<>(initializer, TryFunction.identity(), folder, (folded, pre) -> pre);
  }

  @Override
  public Supplier<Folded<SOURCE, PRETARGET>> supplier() {
    return () -> switch (reference) {
      case null -> {
        synchronized (lock) {
          yield reference = reference == null ? new Folded<SOURCE, PRETARGET>(null, initializer.get()) : reference;
        }
      }
      default -> reference;
    };
  }

  @Override
  public BiConsumer<Folded<SOURCE, PRETARGET>, SOURCE> accumulator() {
    return (acc, elem) -> {
      synchronized (lock) {
        reference = new Folded<>(elem, accumulator.apply(acc.pretarget, transformer.apply(elem)));
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
