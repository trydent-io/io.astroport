package io.citadel.kernel.domain.model;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import java.util.Optional;
import java.util.function.BiFunction;

import static io.citadel.kernel.func.ThrowableBiFunction.noOp;

public final class Snapshots<ID extends Domain.ID<?>, M extends Record & Domain.Model<ID>, A extends Domain.Aggregate, S extends Domain.Snapshot<M, A, S>> implements Domain.Aggregates<ID, M, A>, Task {
  private final EventStore eventStore;
  private final S snapshot;
  private final String name;

  public Snapshots(final EventStore eventStore, final S snapshot, final String name) {
    this.eventStore = eventStore;
    this.snapshot = snapshot;
    this.name = name;
  }

  @Override
  public Future<A> lookup(final ID id, final ThrowablePredicate<? super M> predicate) {
    return eventStore.seek(new Feed.Aggregate(id.toString(), name))
      .map(Feed::stream)
      .map(entries -> entries.reduce(snapshot, this::next, noOp()))
      .compose(filter("Can't solve predicate", it -> predicate.test(it.model())))
      .compose(snapshot -> snapshot.aggregate(eventStore));
  }

  private S next(final S current, final Feed.Entry entry) {
    return current.apply(entry.aggregate().id(), entry.aggregate().version(), entry.event().name(), entry.event().data());
  }
}
