package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Descriptor;
import io.citadel.kernel.domain.State;
import io.citadel.kernel.eventstore.meta.*;
import io.citadel.kernel.eventstore.meta.Feed;
import io.vertx.core.Future;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

final class Aggregates<I, R, E, S extends Enum<S> & State<S, E>> implements Metadata<Context<R, E>> {
  private final Descriptor<I, R, E, S> descriptor;
  private final Metadata<Feed> metadata;

  Aggregates(Descriptor<I, R, E, S> descriptor, Metadata<Feed> metadata) {
    this.descriptor = descriptor;
    this.metadata = metadata;
  }

  @Override
  public Future<Context<R, E>> findEntity(ID id, Name name, Version version) {
    return metadata.findEntity(id, name, version)
      .map(Feed::stream)
      .map(logs -> logs.collect(new Aggregator(descriptor.entry(), descriptor.entity(descriptor.id(id.value())))));
  }

  private final class Aggregator implements Collector<Feed.Log, R, Context<R, E>> {
    private R[] entity;
    private S state;
    private Entity logged;
    @SafeVarargs
    private Aggregator(S state, R... entity) {
      this.state = state;
      this.entity = entity;
    }
    @Override
    public Supplier<R> supplier() {
      return () -> entity[0];
    }
    @Override
    public BiConsumer<R, Feed.Log> accumulator() {
      return this::accumulate;
    }
    private void accumulate(R current, Feed.Log log) {
      final var event = descriptor.event(log.event().name().value(), log.event().data());
      if (state != null) state = state.transit(event);
      entity[0] = descriptor.attach(current, event);
      logged = log.entity();
    }
    @Override
    public BinaryOperator<R> combiner() {
      return (prev, next) -> prev;
    }
    @Override
    public Function<R, Context<R, E>> finisher() {
      return Context.of(entity, state, Transaction.open(vertx, sqlClient, logged));
    }
    @Override
    public Set<Characteristics> characteristics() {
      return null;
    }
  }
}
