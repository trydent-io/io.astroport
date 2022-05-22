package io.citadel.kernel.domain.model;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static io.citadel.kernel.func.ThrowableBiFunction.noOp;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public final class Aggregates<A extends Domain.Aggregate<?, ?>> implements Domain.Lookup<A>, Task {
  private final EventStore eventStore;
  private final Domain.Snapshot<A> snapshot;

  public Aggregates(EventStore eventStore, Domain.Snapshot<A> snapshot) {
    this.eventStore = eventStore;
    this.snapshot = snapshot;
  }

  @Override
  public Future<A> findAggregate(final Domain.ID<?> id, final String name) {
    return eventStore.seek(aggregate(id, name))
      .map(Feed::stream)
      .map(entries -> entries.collect(toAggregate()));
  }

  private Feed.Aggregate aggregate(final Domain.ID<?> id, final String name) {
    return new Feed.Aggregate(id.toString(), name);
  }

  private Aggregation toAggregate() {
    return new Aggregation();
  }

  private final class Aggregation implements Collector<Feed.Entry, Domain.Snapshot<A>[], A> {
    private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);

    @SuppressWarnings("unchecked")
    @Override
    public Supplier<Domain.Snapshot<A>[]> supplier() {
      return () -> (Domain.Snapshot<A>[]) new Domain.Snapshot[]{snapshot};
    }

    @Override
    public BiConsumer<Domain.Snapshot<A>[], Feed.Entry> accumulator() {
      return (snapshot, entry) -> snapshot[0] = snapshot[0].apply(
        entry.aggregate().id(),
        entry.aggregate().version(),
        entry.event().name(),
        entry.event().data()
      );
    }

    @Override
    public BinaryOperator<Domain.Snapshot<A>[]> combiner() {
      return noOp();
    }

    @Override
    public Function<Domain.Snapshot<A>[], A> finisher() {
      return snapshot -> snapshot[0].aggregate(eventStore);
    }

    @Override
    public Set<Characteristics> characteristics() {
      return IdentityFinish;
    }
  }
}
