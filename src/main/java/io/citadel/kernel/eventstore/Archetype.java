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

  private final Aggregate<M> aggregate;
  private final Stream<E> events;

  Archetype(M model, String name, long version, Stream<E> events) {
    this.aggregate = new Aggregate<>(model, name, version);
    this.events = events;
  }

  @Override
  public <S extends Enum<S> & Domain.State<S, E>> Finaltype<M, E, S> hydrate(final S initial, final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
    return events.collect(toFinaltype(initial, hydrator));
  }

  private <S extends Enum<S> & Domain.State<S, E>> ToFinaltype<S> toFinaltype(S initial, ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
    return new ToFinaltype<>(initial, hydrator);
  }

  @SuppressWarnings({"unchecked", "ConstantConditions"})
  private final class ToFinaltype<S extends Enum<S> & Domain.State<S, E>> implements Collector<E, ToFinaltype.Staging<M, S>[], Finaltype<M, E, S>> {
    private record Staging<M extends Record & Domain.Model<?>, S extends Enum<S> & Domain.State<S, ?>>(Aggregate<M> aggregate, S state) {
    }

    private final S initial;
    private final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator;

    private ToFinaltype(final S initial, final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
      this.initial = initial;
      this.hydrator = hydrator;
    }

    @Override
    public Supplier<ToFinaltype.Staging<M, S>[]> supplier() {
      return () -> (ToFinaltype.Staging<M, S>[]) new Object[]{aggregate};
    }

    @Override
    public BiConsumer<ToFinaltype.Staging<M, S>[], E> accumulator() {
      return (aggregates, e) -> aggregates[0] =
        initial == null
          ? staging(e, null)
          : initial.push(e)
          .map(state -> staging(e, state))
          .orElseThrow(() -> new IllegalStateException("Can't apply event"));
    }

    private ToFinaltype.Staging<M, S> staging(final E e, final S state) {
      return new ToFinaltype.Staging<>(new Aggregate<M>(hydrator.apply(aggregate.model(), e), aggregate.name(), aggregate.version()), state);
    }

    @Override
    public BinaryOperator<ToFinaltype.Staging<M, S>[]> combiner() {
      return (stagings, stagings2) -> stagings;
    }

    @Override
    public Function<ToFinaltype.Staging<M, S>[], Finaltype<M, E, S>> finisher() {
      return stagings -> new Finaltype<>(stagings[0].aggregate, stagings[0].state, Stream.empty());
    }

    @Override
    public Set<Characteristics> characteristics() {
      return IdentityFinish;
    }
  }
}

