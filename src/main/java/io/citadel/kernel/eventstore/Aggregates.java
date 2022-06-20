package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Aggregate;
import io.citadel.kernel.domain.State;
import io.citadel.kernel.eventstore.Feed.Event;
import io.citadel.kernel.eventstore.meta.Entity;
import io.citadel.kernel.eventstore.meta.ID;
import io.citadel.kernel.eventstore.meta.Meta;
import io.citadel.kernel.eventstore.meta.Name;
import io.citadel.kernel.eventstore.meta.Version;
import io.vertx.core.Future;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

final class Aggregates<I, R, E, S extends Enum<S> & State<S, E>> implements Lookup<R> {
  private final Aggregate<I, R, E, S> aggregate;
  private final Lookup<Meta> lookup;

  Aggregates(Aggregate<I, R, E, S> aggregate, Lookup<Meta> lookup) {
    this.aggregate = aggregate;
    this.lookup = lookup;
  }

  @Override
  public Future<R> find(ID id, Name name, Version version) {
    return lookup.find(id, name, version)
      .map(Meta::stream)
      .map(logs -> logs.collect(new Aggregator(Entity.of(id, name, version), logs.map(it -> it.event()), aggregate.state())));
  }

  @SuppressWarnings("unchecked")
  private final class Aggregator implements Collector<Meta.Log, R, Object> {
    private final Entity entity;
    private final Stream<Event> events;
    private R record;
    private S state;

    Aggregator(Entity entity, Stream<Event> events, S state) {
      this.entity = entity;
      this.events = events;
      this.state = state;
    }

    @Override
    public Supplier<R> supplier() {
      return () -> {
        record = aggregate
          .entity()
          .apply(
            aggregate
              .id()
              .apply(entity.toString())
          );

        return record;
      };
    }

    @Override
    public BiConsumer<R, Meta.Log> accumulator() {
      return (r, log) -> {
        final E event = aggregate.event().apply(log.event().name(), log.event().data());
        if (state != null) state = state.transit(event);
        record = aggregate.attach().apply(r, event);
      };
    }

    @Override
    public BinaryOperator<R> combiner() {
      return (prev, next) -> prev;
    }

    @Override
    public Function<R, Object> finisher() {
      return null;
    }

    @Override
    public Set<Characteristics> characteristics() {
      return null;
    }
  }
}
