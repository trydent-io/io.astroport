package io.citadel.kernel.domain.model;

import io.citadel.kernel.eventstore.Meta;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.eventstore.Timeline;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static io.citadel.kernel.func.ThrowableBiFunction.noOp;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public final class Aggregates<S extends Domain.State<?, E>, E extends Domain.Event, M extends Domain.Model<?>> implements Domain.Lookup<M, E>, Task {
  private final EventStore eventStore;
  private final Domain.Snapshot<S, E, M> snapshot;

  public Aggregates(EventStore eventStore, Domain.Snapshot<S, E, M> snapshot) {
    this.eventStore = eventStore;
    this.snapshot = snapshot;
  }

  @Override
  public Future<Domain.Transaction<M, E>> findLogs(final Domain.ID<?> aggregateId, final String aggregateName) {
    return eventStore
      .findTimeline(aggregate(aggregateId, aggregateName))
      .map(Meta::stream)
      .map(logs -> logs.collect(toTimeline(aggregateId, aggregateName)))
      .map(timeline -> timeline.)
        //.findFirst().or(elseEmpty(aggregateId, aggregateName)).map(log -> logs.collect(to(log.aggregate()))).orElseThrow());
  }

  private Timeline.ToTimeline toTimeline(final Domain.ID<?> aggregateId, final String aggregateName) {
    return new Timeline.ToTimeline(aggregateId.toString(), aggregateName);
  }

  private Supplier<Optional<? extends Meta.Log>> elseEmpty(Domain.ID<?> id, String name) {
    return () -> Optional.of(Meta.archetype(id.toString(), name));
  }

  private Meta.Aggregate aggregate(final Domain.ID<?> id, final String name) {
    return new Meta.Aggregate(id.toString(), name);
  }

  private Aggregation to(Meta.Aggregate aggregate) {
    return new Aggregation(aggregate.id(), aggregate.version());
  }

  private final class Aggregation implements Collector<Meta.Log, Domain.Snapshot<S, E, M>[], Domain.Transaction<M, E>> {
    private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);
    private final String id;
    private final long version;

    private Aggregation(String id, long version) {
      this.id = id;
      this.version = version;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Supplier<Domain.Snapshot<S, E, M>[]> supplier() {
      return () -> (Domain.Snapshot<S, E, M>[]) new Domain.Snapshot[]{snapshot.archetype(id, version)};
    }

    @Override
    public BiConsumer<Domain.Snapshot<S, E, M>[], Meta.Log> accumulator() {
      return (snapshot, log) -> snapshot[0] = snapshot[0].hydrate(log.event().name(), log.event().data());
    }

    @Override
    public BinaryOperator<Domain.Snapshot<S, E, M>[]> combiner() {
      return noOp();
    }

    @Override
    public Function<Domain.Snapshot<S, E, M>[], Domain.Transaction<M, E>> finisher() {
      return snapshot -> snapshot[0].transaction(eventStore);
    }

    @Override
    public Set<Characteristics> characteristics() {
      return IdentityFinish;
    }
  }
}
