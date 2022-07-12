package io.citadel.kernel.lang.stream;

import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableSupplier;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

@SuppressWarnings("unchecked")
final class Fold<T, R> implements Collector<T, R, R> {
  private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);

  private final R[] reference;
  private final ThrowableSupplier<? extends R> initializer;
  private final ThrowableBiFunction<? super R, ? super T, ? extends R> folder;

  private Fold(R[] reference, ThrowableSupplier<? extends R> initializer, ThrowableBiFunction<? super R, ? super T, ? extends R> folder) {
    this.reference = reference;
    this.initializer = initializer;
    this.folder = folder;
  }

  static <T, R> Fold<T, R> of(ThrowableSupplier<? extends R> initializer, ThrowableBiFunction<? super R, ? super T, ? extends R> folder) {
    return new Fold<>((R[]) new Object[1], initializer, folder);
  }

  @Override
  public Supplier<R> supplier() {
    return () -> reference[0] = initializer.get();
  }

  @Override
  public BiConsumer<R, T> accumulator() {
    return (acc, elem) -> reference[0] = folder.apply(acc, elem);
  }

  @Override
  public BinaryOperator<R> combiner() {
    return (acc1, acc2) -> acc1;
  }

  @Override
  public Function<R, R> finisher() {
    return Function.identity();
  }

  @Override
  public Set<Characteristics> characteristics() {
    return IdentityFinish;
  }
}
