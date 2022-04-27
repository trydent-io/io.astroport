package io.citadel.kernel.domain.repository;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.lang.By;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

import static io.vertx.core.Future.failedFuture;
import static java.util.stream.Collectors.groupingBy;

public final class Repository<A extends Domain.Aggregate, I extends Domain.ID<?>, E extends Domain.Event> implements Domain.Aggregates<A, I, E> {
  private final EventStore eventStore;
  private final Domain.Snapshot<A, I> snapshot;
  private final String name;
  private final ThrowableFunction<? super String, ? extends I> asId;

  public Repository(final EventStore eventStore, final Domain.Snapshot<A, I> snapshot, final String name, final ThrowableFunction<? super String, ? extends I> asId) {
    this.eventStore = eventStore;
    this.snapshot = snapshot;
    this.name = name;
    this.asId = asId;
  }

  @Override
  public Future<A> lookup(final I id) {
    return eventStore.seek(new Feed.Aggregate(id.toString(), name))
      .map(Feed::stream)
      .map(it -> it.collect(By.folding(snapshot, this::next)))
      .map(Domain.Snapshot::aggregate);
  }

  @Override
  public Future<A> persist(final I id, final long version, final Stream<E> events, final String by) {
    return eventStore
      .persist(new Feed.Aggregate(id.toString(), name, version), events.map(it -> new Feed.Event(it.getClass().getSimpleName(), JsonObject.mapFrom(it))), by)
      .map(Feed::stream)
      .map(it -> it.collect(By.folding(snapshot, this::next)))
      .map(Domain.Snapshot::aggregate);
  }

  private Domain.Snapshot<A, I> next(final Domain.Snapshot<A, I> current, final Feed.Entry entry) {
    return current.apply(asId.apply(entry.aggregate().id()), entry.aggregate().version(), entry.event().name(), entry.event().data());
  }
}
