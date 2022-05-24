package io.citadel.kernel.domain.model;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
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

public final class Aggregates<T extends Domain.Transaction<?, ?>> implements Domain.Lookup<T>, Task {
  private final EventStore eventStore;
  private final Domain.Snapshot<T> snapshot;

  public Aggregates(EventStore eventStore, Domain.Snapshot<T> snapshot) {
    this.eventStore = eventStore;
    this.snapshot = snapshot;
  }

  @Override
  public Future<T> openAggregate(final Domain.ID<?> aggregateId, final String aggregateName) {
    return eventStore.seek(aggregate(aggregateId, aggregateName))
      .map(Feed::stream)
      .map(entries -> entries
        .findFirst()
        .or(empty(aggregateId, aggregateName))
        .map(entry -> entries.collect(to(entry.aggregate())))
        .orElseThrow()
      );
  }

  private Supplier<Optional<? extends Feed.Entry>> empty(Domain.ID<?> id, String name) {
    return () -> Optional.of(Feed.archetype(id.toString(), name));
  }

  private Feed.Aggregate aggregate(final Domain.ID<?> id, final String name) {
    return new Feed.Aggregate(id.toString(), name);
  }

  private Aggregation to(Feed.Aggregate aggregate) {
    return new Aggregation(aggregate.id(), aggregate.version());
  }

  private final class Aggregation implements Collector<Feed.Entry, Domain.Snapshot<T>[], T> {
    private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);
    private final String id;
    private final long version;

    private Aggregation(String id, long version) {
      this.id = id;
      this.version = version;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Supplier<Domain.Snapshot<T>[]> supplier() {
      return () -> (Domain.Snapshot<T>[]) new Domain.Snapshot[]{snapshot.archetype(id, version)};
    }

    @Override
    public BiConsumer<Domain.Snapshot<T>[], Feed.Entry> accumulator() {
      return (snapshot, entry) -> snapshot[0] = snapshot[0].hydrate(
        entry.event().name(),
        entry.event().data()
      );
    }

    @Override
    public BinaryOperator<Domain.Snapshot<T>[]> combiner() {
      return noOp();
    }

    @Override
    public Function<Domain.Snapshot<T>[], T> finisher() {
      return snapshot -> snapshot[0].transaction(eventStore);
    }

    @Override
    public Set<Characteristics> characteristics() {
      return IdentityFinish;
    }
  }
}
