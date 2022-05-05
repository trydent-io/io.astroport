package io.citadel.kernel.domain.model;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.lang.By;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import java.util.function.BinaryOperator;

public final class Snapshots<ID extends Domain.ID<?>, M extends Record & Domain.Model<ID>> implements Domain.Models<ID, M>, Task {
  private final EventStore eventStore;
  private final ThrowableFunction<? super ID, ? extends M> identity;
  private final ThrowableBiFunction<? super M, ? super Feed.Entry, ? extends M> reduce;
  private final String name;

  public Snapshots(final EventStore eventStore, final ThrowableFunction<? super ID, ? extends M> identity, final ThrowableBiFunction<? super M, ? super Feed.Entry, ? extends M> reduce, final String name) {
    this.eventStore = eventStore;
    this.identity = identity;
    this.reduce = reduce;
    this.name = name;
  }

  @Override
  public Future<M> lookup(final ID id) {
    return eventStore.seek(new Feed.Aggregate(id.toString(), name))
      .map(Feed::stream)
      .map(entries -> entries.reduce(identity.apply(id), reduce::apply, nothing()));
  }

  @Override
  public Future<M> lookup(final ID id, final ThrowablePredicate<? super M> predicate) {
    return eventStore.seek(new Feed.Aggregate(id.toString(), name))
      .map(Feed::stream)
      .map(entries -> entries.reduce(identity.apply(id), reduce::apply, nothing()))
      .compose(filter(message(id), predicate));
  }

  private BinaryOperator<M> nothing() {
    return (a, b) -> a;
  }

  private String message(final ID id) {
    return "Can't lookup Model %s with ID %s".formatted(name, id);
  }
}
