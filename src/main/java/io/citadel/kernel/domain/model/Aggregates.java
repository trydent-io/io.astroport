package io.citadel.kernel.domain.model;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import static io.citadel.kernel.func.ThrowableBiFunction.noOp;

public final class Aggregates<ID extends Domain.ID<?>, M extends Record & Domain.Model<ID>, A extends Domain.Aggregate, S extends Domain.Snapshot<M, A, S>> implements Domain.Lookup<ID, M, A>, Task {
  private final EventStore eventStore;
  private final S snapshot;
  private final String name;
  private final ThrowablePredicate<? super M> validator;

  public Aggregates(EventStore eventStore, S snapshot, String name) {
    this(eventStore, snapshot, name, it -> true);
  }
  public Aggregates(EventStore eventStore, S snapshot, String name, ThrowablePredicate<? super M> validator) {
    this.eventStore = eventStore;
    this.snapshot = snapshot;
    this.name = name;
    this.validator = validator;
  }


  @Override
  public Future<A> aggregate(final ID id) {
    return eventStore.seek(new Feed.Aggregate(id.toString(), name))
      .map(Feed::stream)
      .map(entries -> entries.reduce(snapshot, this::next, noOp()))
      .compose(filter(it -> it.model().co validator.test(it.model())))
      .compose(snapshot -> snapshot.aggregate(eventStore));
  }

  private S next(final S current, final Feed.Entry entry) {
    return current.apply(entry.aggregate().id(), entry.aggregate().version(), entry.event().name(), entry.event().data());
  }
}
