package io.citadel.kernel.domain.model;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import java.util.Optional;
import java.util.function.BinaryOperator;

public final class Snapshots<ID extends Domain.ID<?>, M extends Record & Domain.Model<ID>, A extends Domain.Aggregate<M>, S extends Domain.Snapshot<M, A, S>> implements Domain.Aggregates<ID, M, A>, Task {
  private final EventStore eventStore;
  private final S snapshot;
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
  public Future<A> lookup(final ID id) {
    return eventStore.seek(new Feed.Aggregate(id.toString(), name))
      .map(Feed::stream)
      .map(entries -> entries.reduce(identity.apply(id), reduce::apply, nothing()));
  }

  @Override
  public Future<A> lookup(final ID id, final ThrowablePredicate<? super M> predicate) {
    return eventStore.seek(new Feed.Aggregate(id.toString(), name))
      .map(Feed::stream)
      .map(entries ->
        entries.<Optional<S>>reduce(
          entries.findFirst().map(entry -> snapshot.identity(entry.aggregate().id())),
          (current, entry) -> current.map(it -> it.event(
              entry.aggregate().version(),
              entry.event().name(),
              entry.event().data()
            )
          ),
          nothing()
        )
      )
      .map(Optional::orElseThrow)
      .compose(filter(message(id), predicate));
  }

  private <U> BinaryOperator<U> nothing() {
    return (a, b) -> a;
  }

  private String message(final ID id) {
    return "Can't lookup Model %s with ID %s".formatted(name, id);
  }
}
