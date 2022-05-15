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

import static io.citadel.kernel.domain.model.Aggregates.Pair.pair;
import static io.citadel.kernel.func.ThrowableBiFunction.noOp;
import static java.util.function.Function.identity;
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

  private record Version<S>(long version, S snapshot) {}

  @Override
  public Future<A> findAggregate(final Domain.ID<?> id, ThrowablePredicate<? super M> verify) {
    return eventStore.seek(new Feed.Aggregate(id.toString(), name))
      .map(Feed::stream)
      .map(entries -> entries.collect(new Collector<Feed.Entry, Domain.Snapshot<M, A>, A>() {
            @Override
            public Supplier<Domain.Snapshot<M, A>> supplier() {
              return () -> snapshot;
            }

            @Override
            public BiConsumer<Domain.Snapshot<M, A>, Feed.Entry> accumulator() {
              return (a, entry) -> a.apply(entry.aggregate().id(), entry.aggregate().version(), entry.event().name(), entry.event().data());
            }

            @Override
            public BinaryOperator<Domain.Snapshot<M, A>> combiner() {
              return noOp();
            }

            @Override
            public Function<Domain.Snapshot<M, A>, A> finisher() {
              return snapshot -> verify.test(snapshot.model()) ? snapshot.aggregate(null) : null;
            }

            @Override
            public Set<Characteristics> characteristics() {
              return Set.of(IDENTITY_FINISH);
            }
          })
        .findFirst()
        .map(first ->  version(first, entries.reduce(snapshot, this::next, noOp())))
        .filter(version -> verify.test(version.snapshot.model()))
      )
      .compose(snapshot -> snapshot.aggregate(eventStore));
  }

  private Version<Domain.Snapshot<M, A>> version(Feed.Entry first, Domain.Snapshot<M, A> snapshot) {
    return new Version<>(first.aggregate().version(), snapshot);
  }

  private Domain.Snapshot<M, A> next(final Domain.Snapshot<M, A> current, final Feed.Entry entry) {
    return current.apply(entry.aggregate().id(), entry.aggregate().version(), entry.event().name(), entry.event().data());
  }
}
