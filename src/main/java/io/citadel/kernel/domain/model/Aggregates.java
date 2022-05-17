package io.citadel.kernel.domain.model;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowablePredicate;
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

public final class Aggregates<M extends Record & Domain.Model<?>, A extends Domain.Aggregate> implements Domain.Lookup<M, A>, Task {
  private final EventStore eventStore;
  private final Domain.Snapshot<M, A> snapshot;
  private final String name;

  public Aggregates(EventStore eventStore, Domain.Snapshot<M, A> snapshot, String name) {
    this.eventStore = eventStore;
    this.snapshot = snapshot;
    this.name = name;
  }

  @Override
  public Future<A> findAggregate(final Domain.ID<?> id, ThrowablePredicate<? super M> verify) {
    return eventStore.seek(aggregate(id))
      .map(Feed::stream)
      .map(entries -> entries.collect(toAggregate(verify)))
      .compose(requireNonNull("Can't match aggregate with provided verification"));
  }

  private Feed.Aggregate aggregate(final Domain.ID<?> id) {
    return new Feed.Aggregate(id.toString(), name);
  }

  private Aggregation toAggregate(ThrowablePredicate<? super M> verify) {
    return new Aggregation(verify);
  }

  private final class Aggregation implements Collector<Feed.Entry, Domain.Snapshot<M, A>[], A> {
    private static final Set<Characteristics> CHARACTERISTIC = Set.of(IDENTITY_FINISH);
    private final ThrowablePredicate<? super M> verify;

    public Aggregation(ThrowablePredicate<? super M> verify) {
      this.verify = verify;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Supplier<Domain.Snapshot<M, A>[]> supplier() {
      return () -> (Domain.Snapshot<M, A>[]) new Domain.Snapshot[]{snapshot};
    }

    @Override
    public BiConsumer<Domain.Snapshot<M, A>[], Feed.Entry> accumulator() {
      return (snapshot, entry) -> snapshot[0] = snapshot[0].apply(
        entry.aggregate().id(),
        entry.aggregate().version(),
        entry.event().name(),
        entry.event().data()
      );
    }

    @Override
    public BinaryOperator<Domain.Snapshot<M, A>[]> combiner() {
      return noOp();
    }

    @Override
    public Function<Domain.Snapshot<M, A>[], A> finisher() {
      return snapshot -> snapshot[0].aggregate(eventStore, verify);
    }

    @Override
    public Set<Characteristics> characteristics() {
      return CHARACTERISTIC;
    }
  }
}
