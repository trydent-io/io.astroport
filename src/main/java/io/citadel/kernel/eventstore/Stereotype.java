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

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public final class Stereotype<M extends Record & Domain.Model<?>, E extends Domain.Event> {

  private final Aggregate<M> aggregate;
  private final Stream<E> events;

  Stereotype(final Aggregate<M> aggregate, final Stream<E> events) {
    this.aggregate = aggregate;
    this.events = events;
  }

  public <S extends Enum<S> & Domain.State<S, E>> Finaltype<M, E, S> hydrate(ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
    return hydrate(null, hydrator);
  }

  public <S extends Enum<S> & Domain.State<S, E>> Finaltype<M, E, S> hydrate(S initial, ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
    return events.collect(toFinaltype(initial, hydrator));
  }

  private <S extends Enum<S> & Domain.State<S, E>> ToFinaltype<S> toFinaltype(S initial, ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
    return new ToFinaltype<>(initial, hydrator);
  }

  @SuppressWarnings("unchecked")
  private final class ToFinaltype<S extends Enum<S> & Domain.State<S, E>> implements Collector<E, ToFinaltype.Staging<M, S>[], Finaltype<M, E, S>> {
    private record Staging<M extends Record & Domain.Model<?>, S extends Enum<S> & Domain.State<S, ?>>(Aggregate<M> aggregate, S state) {}

    private final S initial;
    private final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator;

    private ToFinaltype(final S initial, final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
      this.initial = initial;
      this.hydrator = hydrator;
    }

    @Override
    public Supplier<Staging<M, S>[]> supplier() {
      return () -> (Staging<M, S>[]) new Object[]{aggregate};
    }

    @Override
    public BiConsumer<Staging<M, S>[], E> accumulator() {
      return (aggregates, e) -> aggregates[0] = initial == null
        ? staging(e, null)
        : initial.push(e)
            .map(state -> staging(e, state))
            .orElseThrow(() -> new IllegalStateException("Can't apply event"));
    }

    private Staging<M, S> staging(final E e, final S state) {
      return new Staging<>(new Aggregate<M>(hydrator.apply(aggregate.model(), e), aggregate.name(), aggregate.version()), state);
    }

    @Override
    public BinaryOperator<Staging<M, S>[]> combiner() {
      return (stagings, stagings2) -> stagings;
    }

    @Override
    public Function<Staging<M, S>[], Finaltype<M, E, S>> finisher() {
      return stagings -> new Finaltype<>(stagings[0].aggregate, stagings[0].state, Stream.empty());
    }

    @Override
    public Set<Characteristics> characteristics() {
      return IdentityFinish;
    }
  }
}
