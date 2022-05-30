package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableBiFunction;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collector.Characteristics;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public final class Archetype<M extends Record & Domain.Model<?>, E extends Domain.Event> implements Identity<M, E> {
  private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);

  private final M model;
  private final String name;
  private final long version;
  private final Stream<E> events;

  Archetype(M model, String name, long version, Stream<E> events) {
    this.model = model;
    this.name = name;
    this.version = version;
    this.events = events;
  }

  @Override
  public <S extends Enum<S> & Domain.State<S, E>> Aggregate<M, E, S> hydrate(final S initial, final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
    return events.collect(toAggregate(initial, hydrator));
  }

  private <S extends Enum<S> & Domain.State<S, E>> ToAggregate<S> toAggregate(S initial, ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
    return new ToAggregate<>(initial, hydrator);
  }

  @SuppressWarnings({"unchecked", "ConstantConditions"})
  private final class ToAggregate<S extends Enum<S> & Domain.State<S, E>> implements Collector<E, ToAggregate.Staging<M, S>[], Aggregate<M, E, S>> {
    private record Staging<M extends Record & Domain.Model<?>, S extends Enum<S> & Domain.State<S, ?>>(M model, S state) {
    }

    private final S initial;
    private final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator;

    private ToAggregate(final S initial, final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
      this.initial = initial;
      this.hydrator = hydrator;
    }

    @Override
    public Supplier<ToAggregate.Staging<M, S>[]> supplier() {
      return () -> (ToAggregate.Staging<M, S>[]) new Object[]{model};
    }

    @Override
    public BiConsumer<ToAggregate.Staging<M, S>[], E> accumulator() {
      return (aggregates, e) -> aggregates[0] =
        initial == null
          ? staging(e, null)
          : initial.push(e)
          .map(state -> staging(e, state))
          .orElseThrow(() -> new IllegalStateException("Can't apply event"));
    }

    private ToAggregate.Staging<M, S> staging(final E e, final S state) {
      return new ToAggregate.Staging<>(hydrator.apply(model, e), state);
    }

    @Override
    public BinaryOperator<ToAggregate.Staging<M, S>[]> combiner() {
      return (stagings, stagings2) -> stagings;
    }

    @Override
    public Function<ToAggregate.Staging<M, S>[], Aggregate<M, E, S>> finisher() {
      return stagings -> new Aggregate<>(stagings[0].model, name, version, stagings[0].state);
    }

    @Override
    public Set<Characteristics> characteristics() {
      return IdentityFinish;
    }
  }
}

