package io.citadel.kernel.domain.repository;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.eventstore.data.AggregateInfo;
import io.citadel.kernel.eventstore.data.EventInfo;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.lang.By;
import io.vertx.core.Future;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.vertx.core.Future.failedFuture;
import static java.util.stream.Collectors.groupingBy;

public final class Repository<A extends Domain.Aggregate, I extends Domain.ID<?>, E extends Domain.Event, S extends Domain.Seed<?>> implements Domain.Aggregates<A, I> {
  private final EventStore eventStore;

  private final ThrowableFunction<? super String, ? extends S> identity;
  private final ThrowableBiFunction<? super Domain.Seed<?>, ? super E, ? extends S> reduce;
  private final ThrowableBiFunction<? super String, ? super Long, ? extends Domain.Seed<?>> snapshot;
  private final ThrowableBiFunction<? super String, ? super String, ? extends E> asEvent;
  private final Thro
  private final String name;

  public Repository(final EventStore eventStore, final Domain.Snapshot<A> snapshot, final String name) {
    this.eventStore = eventStore;
    this.snapshot = snapshot;
    this.name = name;
  }

  @Override
  public Future<A> lookup(final I id) {
    return eventStore.seek(new Feed.Aggregate(id.toString(), name))
      .map(Feed::stream)
      .map(asSnapshot())
      .map(it -> it.collect(By.folding()))
      .compose(aggregate -> aggregate
        .map(Future::succeededFuture)
        .orElse(failedFuture("Can't hydrate root %s with id %s".formatted(name, id)))
      );
  }

  private record Local(Feed.Aggregate aggregate, Stream<Feed.Entry> entries) {
    private Local(Map.Entry<Feed.Aggregate, List<Feed.Entry>> entry) { this(entry.getKey(), entry.getValue().stream()); }
  }

  private Function<Stream<Feed.Entry>, S> asSnapshot() {
    return entries -> groupByAggregate(entries)
      .map(Local::new)
      .map(asReduce())
      .findFirst()
      .orElseThrow();
  }

  private Function<Local, ? extends S> asReduce() {
    return local -> local
      .entries
      .map(Feed.Entry::event)
      .map(event -> asEvent.apply(event.name(), event.data()))
      .collect(By.folding(identity.apply(local.aggregate.id()), reduce));
  }

  private Stream<Map.Entry<Feed.Aggregate, List<Feed.Entry>>> groupByAggregate(final Stream<Feed.Entry> entries) {
    return entries
      .collect(groupingBy(Feed.Entry::aggregate))
      .entrySet()
      .stream();
  }

  @Override
  public Future<Void> persist(final AggregateInfo aggregate, final Stream<EventInfo> events, final String by) {
    return eventStore
      .persist(aggregate, events, by)
      .mapEmpty();
  }
}
