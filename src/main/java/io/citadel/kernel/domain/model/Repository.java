package io.citadel.kernel.domain.model;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.lang.By;
import io.vertx.core.Future;

public final class Repository<A extends Domain.Aggregate, I extends Domain.ID<?>, M extends Record> implements Domain.Aggregates<A, I, M> {
  private final EventStore eventStore;
  private final Domain.Snapshot<A, M> snapshot;
  private final String name;

  public Repository(final EventStore eventStore, final Domain.Snapshot<A, M> snapshot, final String name) {
    this.eventStore = eventStore;
    this.snapshot = snapshot;
    this.name = name;
  }

  @Override
  public Future<A> lookup(final I id) {
    return hydratingBy(id).map(snapshot -> snapshot.aggregate(Defaults.transaction(eventStore)));
  }

  @Override
  public Future<A> lookup(I id, ThrowablePredicate<? super M> with) {
    return hydratingBy(id).map(snapshot -> snapshot.aggregate(with));
  }

  private Future<Domain.Snapshot<A, M>> hydratingBy(final I id) {
    return eventStore.seek(new Feed.Aggregate(id.toString(), name))
      .map(Feed::stream)
      .map(it -> it.collect(By.reducing(snapshot, this::next)));
  }

  private Domain.Snapshot<A, M> next(final Domain.Snapshot<A, M> current, final Feed.Entry entry) {
    return current.apply(entry.aggregate().id(), entry.aggregate().version(), entry.event().name(), entry.event().data());
  }
}
