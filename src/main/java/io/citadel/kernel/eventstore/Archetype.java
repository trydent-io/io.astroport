package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Domain.State;
import io.citadel.kernel.func.ThrowableTriFunction;
import io.vertx.core.json.JsonObject;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public interface Archetype<M extends Record & Domain.Model<?>> {

  static <S extends Enum<S> & State<S, ?>, M extends Record & Domain.Model<?>> Archetype<M> identity(M model, Stream<Feed.Event> events, long version) {
    return new Identity<>(null, model, events, version);
  }

  <S extends Enum<S> & State<S, ?>> Archetype<M> initial(S state);
  <E extends Domain.Event> Domain.Transaction<M, E> hydrate(ThrowableTriFunction<? super M, ? super String, ? super JsonObject, ? extends M> function);

  final class Identity<S extends Enum<S> & State<S, ?>, M extends Record & Domain.Model<?>> implements Archetype<M> {
    private final S state;
    private final M model;
    private final Stream<Feed.Event> events;
    private final long version;

    private Identity(final S state, final M model, final Stream<Feed.Event> events, final long version) {
      this.state = state;
      this.model = model;
      this.events = events;
      this.version = version;
    }

    @Override
    public <I extends Enum<I> & State<I, ?>> Archetype<M> initial(final I state) {
      return new Identity<>(state, model, events, version);
    }

    @Override
    public <E extends Domain.Event> Domain.Transaction<M, E> hydrate(final ThrowableTriFunction<? super M, ? super String, ? super JsonObject, ? extends M> function) {
      return events.collect(new Aggregation<>(function));
    }

    final class Aggregation<E extends Domain.Event> implements Collector<Feed.Event, M[], Domain.Transaction<M, E>> {
      private final ThrowableTriFunction<? super M, ? super String, ? super JsonObject, ? extends M> function;

      Aggregation(final ThrowableTriFunction<? super M, ? super String, ? super JsonObject, ? extends M> function) {this.function = function;}

      @SuppressWarnings("unchecked")
      @Override
      public Supplier<M[]> supplier() {
        return () -> (M[]) new Object[] { model };
      }

      @Override
      public BiConsumer<M[], Feed.Event> accumulator() {
        return (ms, event) -> ms[0] = function.apply(ms[0], event.name(), event.data());
      }

      @Override
      public BinaryOperator<M[]> combiner() {
        return (ms, ms2) -> ms;
      }

      @Override
      public Function<M[], Domain.Transaction<M, E>> finisher() {
        return ms -> null;
      }

      @Override
      public Set<Characteristics> characteristics() {
        return Set.of(IDENTITY_FINISH);
      }
    }
  }
}
